let state = { cellars: [] };

function generateId(prefix) {
  return `${prefix}_${Date.now()}_${Math.floor(Math.random() * 100000)}`;
}

async function init() {
  state = await window.wineCellarAPI.loadData();
  bindEvents();
  render();
}

function bindEvents() {
  document.getElementById("createCellarBtn").addEventListener("click", createCellar);
  document.getElementById("addBottleBtn").addEventListener("click", addBottle);
  document.getElementById("searchInput").addEventListener("input", render);
}

function createCellar() {
  const name = document.getElementById("cellarName").value.trim();
  const rackCount = parseInt(document.getElementById("rackCount").value, 10);

  if (!name || !rackCount || rackCount < 1) {
    alert("Enter a valid cellar name and rack count.");
    return;
  }

  const cellar = {
    id: generateId("cellar"),
    name,
    racks: Array.from({ length: rackCount }, (_, i) => ({
      id: generateId("rack"),
      number: i + 1,
      bottles: []
    }))
  };

  state.cellars.push(cellar);
  persistAndRender();
  document.getElementById("cellarName").value = "";
}

function getSelectedCellar() {
  const cellarId = document.getElementById("cellarSelect").value;
  return state.cellars.find(c => c.id === cellarId);
}

function addBottle() {
  const cellar = getSelectedCellar();
  if (!cellar) {
    alert("Please create or select a cellar.");
    return;
  }

  const rackId = document.getElementById("rackSelect").value;
  const rack = cellar.racks.find(r => r.id === rackId);
  if (!rack) {
    alert("Please select a rack.");
    return;
  }

  const bottle = {
    id: generateId("bottle"),
    name: document.getElementById("wineName").value.trim(),
    producer: document.getElementById("producer").value.trim(),
    origin: document.getElementById("origin").value.trim(),
    vintage: document.getElementById("vintage").value.trim(),
    type: document.getElementById("wineType").value.trim(),
    quantity: parseInt(document.getElementById("quantity").value, 10) || 1,
    aiPrice: null,
    aiPriceConfidence: null,
    tastingNote: "",
    professionalStyleSummary: "",
    drinkWindow: "",
    rationale: "",
    aiUpdatedAt: null
  };

  if (!bottle.name) {
    alert("Wine name is required.");
    return;
  }

  rack.bottles.push(bottle);
  persistAndRender();
  clearBottleForm();
}

function clearBottleForm() {
  document.getElementById("wineName").value = "";
  document.getElementById("producer").value = "";
  document.getElementById("origin").value = "";
  document.getElementById("vintage").value = "";
  document.getElementById("wineType").value = "";
  document.getElementById("quantity").value = "1";
}

async function enrichBottle(cellarId, rackId, bottleId) {
  const cellar = state.cellars.find(c => c.id === cellarId);
  const rack = cellar?.racks.find(r => r.id === rackId);
  const bottle = rack?.bottles.find(b => b.id === bottleId);

  if (!bottle) return;

  const result = await window.wineCellarAPI.enrichBottle(bottle);

  if (!result.ok) {
    alert(result.error || "AI enrichment failed.");
    return;
  }

  bottle.aiPrice = result.data.estimatedPriceUsd;
  bottle.aiPriceConfidence = result.data.priceConfidence;
  bottle.tastingNote = result.data.tastingNote;
  bottle.professionalStyleSummary = result.data.professionalStyleSummary;
  bottle.drinkWindow = result.data.drinkWindow;
  bottle.rationale = result.data.rationale;
  bottle.aiUpdatedAt = new Date().toISOString();

  persistAndRender();
}

function decrementStock(cellarId, rackId, bottleId) {
  const cellar = state.cellars.find(c => c.id === cellarId);
  const rack = cellar?.racks.find(r => r.id === rackId);
  const bottle = rack?.bottles.find(b => b.id === bottleId);

  if (!bottle) return;
  if (bottle.quantity > 0) {
    bottle.quantity -= 1;
  }

  persistAndRender();
}

function matchesSearch(bottle, search) {
  if (!search) return true;
  const fields = [
    bottle.name,
    bottle.producer,
    bottle.origin,
    bottle.vintage,
    bottle.type
  ].join(" ").toLowerCase();

  return fields.includes(search.toLowerCase());
}

function render() {
  renderSelectors();
  renderCellars();
}

function renderSelectors() {
  const cellarSelect = document.getElementById("cellarSelect");
  const rackSelect = document.getElementById("rackSelect");

  cellarSelect.innerHTML = state.cellars.map(cellar =>
    `<option value="${cellar.id}">${escapeHtml(cellar.name)}</option>`
  ).join("");

  const selectedCellar = getSelectedCellar() || state.cellars[0];

  if (!selectedCellar) {
    rackSelect.innerHTML = "";
    return;
  }

  cellarSelect.value = selectedCellar.id;

  rackSelect.innerHTML = selectedCellar.racks.map(rack =>
    `<option value="${rack.id}">Rack ${rack.number}</option>`
  ).join("");
}

function renderCellars() {
  const container = document.getElementById("cellarsContainer");
  const search = document.getElementById("searchInput").value.trim();

  if (state.cellars.length === 0) {
    container.innerHTML = `<div class="success">No cellars yet. Create one above.</div>`;
    return;
  }

  container.innerHTML = state.cellars.map(cellar => `
    <div class="cellar-card">
      <div class="cellar-title">${escapeHtml(cellar.name)}</div>
      ${cellar.racks.map(rack => {
        const visibleBottles = rack.bottles.filter(b => matchesSearch(b, search));
        return `
          <div class="rack">
            <div class="rack-title">Rack ${rack.number} - ${rack.bottles.length} bottle record(s)</div>
            ${
              visibleBottles.length === 0
                ? `<div class="meta" style="color:#ddd;">No matching bottles.</div>`
                : `
                  <div class="bottle-list">
                    ${visibleBottles.map(bottle => `
                      <div class="bottle-card">
                        <div class="bottle-name">${escapeHtml(bottle.name)}</div>
                        <div class="meta"><b>Producer:</b> ${escapeHtml(bottle.producer || "Unknown")}</div>
                        <div class="meta"><b>Origin:</b> ${escapeHtml(bottle.origin || "Unknown")}</div>
                        <div class="meta"><b>Vintage:</b> ${escapeHtml(bottle.vintage || "Unknown")}</div>
                        <div class="meta"><b>Type:</b> ${escapeHtml(bottle.type || "Unknown")}</div>
                        <div class="meta"><b>Quantity:</b> ${bottle.quantity}</div>

                        <div class="ai-box">
                          <div class="meta"><b>AI Estimated Price:</b> ${bottle.aiPrice ?? "Not updated"}</div>
                          <div class="meta"><b>Price Confidence:</b> ${escapeHtml(bottle.aiPriceConfidence || "N/A")}</div>
                          <div class="meta"><b>Drink Window:</b> ${escapeHtml(bottle.drinkWindow || "N/A")}</div>
                          <div class="meta"><b>Tasting Note:</b> ${escapeHtml(bottle.tastingNote || "N/A")}</div>
                          <div class="meta"><b>Professional Summary:</b> ${escapeHtml(bottle.professionalStyleSummary || "N/A")}</div>
                          <div class="meta"><b>Rationale:</b> ${escapeHtml(bottle.rationale || "N/A")}</div>
                          <div class="meta"><b>Last AI Update:</b> ${escapeHtml(bottle.aiUpdatedAt || "Never")}</div>
                        </div>

                        <div class="actions">
                          <button onclick="enrichBottle('${cellar.id}', '${rack.id}', '${bottle.id}')">Update AI Price & Notes</button>
                          <button onclick="decrementStock('${cellar.id}', '${rack.id}', '${bottle.id}')">Consume 1 Bottle</button>
                        </div>
                      </div>
                    `).join("")}
                  </div>
                `
            }
          </div>
        `;
      }).join("")}
    </div>
  `).join("");
}

async function persistAndRender() {
  await window.wineCellarAPI.saveData(state);
  render();
}

function escapeHtml(text) {
  return String(text)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

init();
const { app, BrowserWindow, ipcMain } = require("electron");
const path = require("path");
const fs = require("fs");
const dotenv = require("dotenv");

dotenv.config();

const DATA_PATH = path.join(__dirname, "data", "cellar-data.json");

function ensureDataFile() {
  const dir = path.dirname(DATA_PATH);
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }

  if (!fs.existsSync(DATA_PATH)) {
    fs.writeFileSync(
      DATA_PATH,
      JSON.stringify({ cellars: [] }, null, 2),
      "utf-8"
    );
  }
}

function readData() {
  ensureDataFile();
  const raw = fs.readFileSync(DATA_PATH, "utf-8");
  return JSON.parse(raw);
}

function writeData(data) {
  ensureDataFile();
  fs.writeFileSync(DATA_PATH, JSON.stringify(data, null, 2), "utf-8");
}

function createWindow() {
  const win = new BrowserWindow({
    width: 1400,
    height: 950,
    webPreferences: {
      preload: path.join(__dirname, "preload.js"),
      contextIsolation: true,
      nodeIntegration: false
    }
  });

  win.loadFile(path.join(__dirname, "src", "index.html"));
}

app.whenReady().then(() => {
  ensureDataFile();
  createWindow();

  app.on("activate", function () {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on("window-all-closed", function () {
  if (process.platform !== "darwin") app.quit();
});

ipcMain.handle("data:load", async () => {
  return readData();
});

ipcMain.handle("data:save", async (_event, newData) => {
  writeData(newData);
  return { ok: true };
});

ipcMain.handle("gemini:enrichBottle", async (_event, bottle) => {
  const apiKey = process.env.GEMINI_API_KEY;
  const model = process.env.GEMINI_MODEL || "gemini-2.5-flash";

  if (!apiKey) {
    return {
      ok: false,
      error: "Missing GEMINI_API_KEY in .env file."
    };
  }

  const schema = {
    type: "object",
    properties: {
      estimatedPriceUsd: { type: "number" },
      priceConfidence: { type: "string" },
      tastingNote: { type: "string" },
      professionalStyleSummary: { type: "string" },
      drinkWindow: { type: "string" },
      rationale: { type: "string" }
    },
    required: [
      "estimatedPriceUsd",
      "priceConfidence",
      "tastingNote",
      "professionalStyleSummary",
      "drinkWindow",
      "rationale"
    ]
  };

  const prompt = `
You are assisting a wine cellar management desktop application.

Estimate the current bottle price in USD and write a professional-style tasting note.
Use cautious language where uncertain.
Treat the price as an estimate, not guaranteed market truth.

Bottle data:
- Wine name: ${bottle.name || ""}
- Producer: ${bottle.producer || ""}
- Origin: ${bottle.origin || ""}
- Vintage: ${bottle.vintage || ""}
- Type: ${bottle.type || ""}
- Quantity in stock: ${bottle.quantity || 0}

Return only valid JSON matching the requested schema.
`.trim();

  try {
    const response = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          contents: [
            {
              parts: [
                {
                  text: prompt
                }
              ]
            }
          ],
          generationConfig: {
            responseMimeType: "application/json",
            responseSchema: schema
          }
        })
      }
    );

    if (!response.ok) {
      const errorText = await response.text();
      return {
        ok: false,
        error: `Gemini API error: ${errorText}`
      };
    }

    const result = await response.json();
    const text =
      result?.candidates?.[0]?.content?.parts?.[0]?.text || "";

    let parsed;
    try {
      parsed = JSON.parse(text);
    } catch (err) {
      return {
        ok: false,
        error: "Gemini response was not valid JSON.",
        raw: text
      };
    }

    return {
      ok: true,
      data: parsed
    };
  } catch (error) {
    return {
      ok: false,
      error: error.message
    };
  }
});
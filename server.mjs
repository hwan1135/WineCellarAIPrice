import express from "express";
import dotenv from "dotenv";
import { GoogleGenAI } from "@google/genai";

dotenv.config();

const app = express();
const port = process.env.PORT || 3000;

if (!process.env.GEMINI_API_KEY) {
  console.error("ERROR: GEMINI_API_KEY is missing from the .env file.");
  process.exit(1);
}

if (!process.env.GEMINI_MODEL) {
  console.error("ERROR: GEMINI_MODEL is missing from the .env file.");
  process.exit(1);
}

const ai = new GoogleGenAI({
  apiKey: process.env.GEMINI_API_KEY
});

app.use(express.json({ limit: "5mb" }));
app.use(express.static("public"));

app.post("/api/wine-research", async (req, res) => {
  try {
    const wine = req.body;

    const prompt = `
You are a wine research assistant.

Research the wine described below using Google Search when useful. Provide a concise, professional-style research report. Prefer official producer, importer, appellation, and reputable wine-publication information.

Do not invent facts. Clearly state when the exact wine, vintage, producer, region, or technical details cannot be verified.

WINE DETAILS PROVIDED BY USER

Producer / Winery:
${wine.producer || "Not provided"}

Wine Name / Cuvée:
${wine.wineName || "Not provided"}

Vintage:
${wine.vintage || "Not provided"}

Country:
${wine.country || "Not provided"}

Region / Appellation:
${wine.region || "Not provided"}

Grape / Blend:
${wine.grape || "Not provided"}

USER TASTING NOTES

Appearance:
${wine.appearance || "Not provided"}

Nose / Aroma:
${wine.nose || "Not provided"}

Palate:
${wine.palate || "Not provided"}

Finish:
${wine.finish || "Not provided"}

Food Pairing Tried:
${wine.pairing || "Not provided"}

Overall Notes:
${wine.notes || "Not provided"}

Create the report using these headings:

1. Wine Identification
2. Producer and Region
3. Grape Variety or Blend
4. Typical Professional Tasting Profile
5. Structure and Style
6. Serving Temperature and Decanting Guidance
7. Food Pairing Suggestions
8. Cellaring or Drinking Window
9. Comparison to the User's Tasting Notes
10. Sources and Verification Notes

For the "Sources and Verification Notes" section:
- Identify the most useful public sources found.
- State whether the information is from an official producer/importer source, retailer, publication, or other source.
- Clearly distinguish verified details from general wine-style guidance.
`;

    const interaction = await ai.interactions.create({
      model: process.env.GEMINI_MODEL,
      input: prompt,
      tools: [
        {
          type: "google_search"
        }
      ]
    });

    const report =
      interaction.output_text ||
      "Gemini did not return a research report.";

    res.json({
      success: true,
      report: report
    });

  } catch (error) {
    console.error("Gemini research error:", error);

    res.status(500).json({
      success: false,
      message: "Unable to retrieve Gemini wine research at this time."
    });
  }
});

app.listen(port, () => {
  console.log(`Wine Taste Note Tracker running at http://localhost:${port}`);
});
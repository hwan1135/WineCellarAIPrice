# Wine Cellar Electron Desktop App

Windows desktop starter app for wine cellar management with Gemini AI enrichment.

## Features

- Multiple cellars
- Multiple racks per cellar
- Bottle inventory tracking
- Quantity / stock tracking
- Search bottles
- AI enrichment using Gemini:
  - estimated latest bottle price (USD)
  - professional-style tasting note
  - summary
  - drink window
  - rationale
- Local persistence in JSON file

## Requirements

- Windows desktop
- Node.js installed
- Gemini API key

## Setup

1. Clone or copy this repository
2. Run:

```bash
npm install
Create a .env file based on .env.example

Example:

GEMINI_API_KEY=your_key_here
GEMINI_MODEL=gemini-2.5-flash
Start the app:

npm start
Data location
The app stores local data in:

data/cellar-data.json
Gemini integration
This app calls Gemini generateContent with structured JSON output using responseMimeType and responseSchema.

Important note
AI price is an estimate and should not be treated as authoritative live market pricing.
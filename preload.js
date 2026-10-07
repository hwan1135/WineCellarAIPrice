const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("wineCellarAPI", {
  loadData: () => ipcRenderer.invoke("data:load"),
  saveData: (data) => ipcRenderer.invoke("data:save", data),
  enrichBottle: (bottle) => ipcRenderer.invoke("gemini:enrichBottle", bottle)
});
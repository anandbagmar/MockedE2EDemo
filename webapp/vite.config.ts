import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Relative base keeps asset URLs working both locally (`npm run dev`/`preview`)
// and when served from a GitHub Pages project sub-path
// (https://<user>.github.io/MockedE2EDemo/). Combined with HashRouter, no
// server-side rewrites are required.
export default defineConfig({
  base: './',
  plugins: [react()],
  server: {
    port: 5173,
    host: true,
  },
});

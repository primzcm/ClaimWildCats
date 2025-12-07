/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        cream: '#FFFCF5',
        burgundy: '#5B0000',
        burgundyDeep: '#3D0000',
        brown: '#2D1A12',
        gold: '#F2C046',
        sand: '#F7EBDB',
      },
      fontFamily: {
        sans: ['Inter', '"Segoe UI"', 'system-ui', 'sans-serif'],
      },
      boxShadow: {
        soft: '0 18px 48px -24px rgba(91, 0, 0, 0.35)',
        card: '0 16px 36px -20px rgba(0, 0, 0, 0.22)',
      },
    },
  },
  plugins: [],
};

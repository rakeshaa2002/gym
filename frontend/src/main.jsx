import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';
import './index.scss';
import App from './App';
import React from 'react';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
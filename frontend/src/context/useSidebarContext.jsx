// src/context/SidebarContext.js
import React, { createContext, useState, useContext, useEffect } from 'react';

const SidebarContext = createContext();

export const useSidebarContext = () => useContext(SidebarContext);

export const SidebarProvider = ({ children }) => {
  const [isCompact, setIsCompact] = useState(false);

  const toggleSidebar = () => {
    setIsCompact(prev => !prev);
  };

  useEffect(() => {
    if (isCompact) {
      document.body.setAttribute('data-bs-sidebar', 'compact');
    } else {
      document.body.removeAttribute('data-bs-sidebar');
    }
  }, [isCompact]);

  return (
    <SidebarContext.Provider value={{ isCompact, toggleSidebar }}>
      {children}
    </SidebarContext.Provider>
  );
};
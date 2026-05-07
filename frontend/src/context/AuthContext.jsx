import React, { createContext, useContext, useState, useEffect } from "react";
import { setAccessToken, clearTokens } from "../utils/api";

const AuthContext = createContext();
function clearStoredAuth() {
  clearTokens();
  localStorage.removeItem("token");
  localStorage.removeItem("accessToken");
  localStorage.removeItem("email");
  localStorage.removeItem("name");
  localStorage.removeItem("role");
  localStorage.removeItem("userId");
}

function decodeJwtPayload(token) {
  const encodedPayload = token.split(".")[1];
  if (!encodedPayload) return null;

  const base64 = encodedPayload.replace(/-/g, "+").replace(/_/g, "/");
  const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), "=");
  return JSON.parse(atob(padded));
}

function isJwtUsable(token) {
  if (!token) return false;

  try {
    const payload = decodeJwtPayload(token);
    if (!payload?.exp) return true;
    return payload.exp * 1000 > Date.now();
  } catch {
    return false;
  }
}

export function AuthProvider({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("token") || localStorage.getItem("accessToken");
    const email = localStorage.getItem("email");
    const name = localStorage.getItem("name");
    const role = localStorage.getItem("role");
    const userId = localStorage.getItem("userId");

    if (isJwtUsable(token)) {
      setAccessToken(token);
      setIsAuthenticated(true);
      setUser({ email, name, role, userId });
    } else {
      clearStoredAuth();
      setIsAuthenticated(false);
      setUser(null);
    }

    setLoading(false);
  }, []);

  const login = (userData) => {
    const token = userData.token || userData.accessToken || "";

    setUser(userData);
    setIsAuthenticated(true);

    setAccessToken(token);

    localStorage.setItem("token", token);
    localStorage.setItem("accessToken", token);
    localStorage.setItem("email", userData.email || "");
    localStorage.setItem("name", userData.name || "");
    localStorage.setItem("role", userData.role || "");
    localStorage.setItem("userId", userData.userId || "");
  };

  const logout = () => {
    setUser(null);
    setIsAuthenticated(false);

    clearStoredAuth();
  };

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        user,
        loading,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
}



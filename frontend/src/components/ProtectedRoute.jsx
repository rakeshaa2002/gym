import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext'; // Adjust path as needed
import Sidebar from './Sidebar';
import Header from './Header';
import MobileBottomNav from './MobileBottomNav';
import useIsMobile from '../hooks/useIsMobile';
import { findPageByPath } from '../config/pagePermissions';

export default function ProtectedRoute() {
    const { isAuthenticated, loading, permissionsLoading, hasPermission } = useAuth();
    const isMobile = useIsMobile();
    const location = useLocation();

    // Show loading while checking auth
    if (loading || permissionsLoading) {
        console.log("ProtectedRoute - Still checking authentication...");
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <div className="spinner-border" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
            </div>
        );
    }

    // If not authenticated, redirect to login
    if (!isAuthenticated) {
        console.warn("ProtectedRoute - Not authenticated, redirecting to /sign-in");
        return <Navigate to="/sign-in" replace />;
    }

    const currentPage = findPageByPath(location.pathname);
    if (currentPage && !hasPermission(currentPage.key, "view")) {
        return <Navigate to="/error-page" replace />;
    }

    // If authenticated, render the nested routes
    console.log("ProtectedRoute - Authenticated, rendering protected content");
    return (
    <div className="codex-main">
        <Sidebar />
        <Header />
        <div className="codex-content">
            <Outlet />
        </div>
        {isMobile ? <MobileBottomNav /> : null}
    </div>
);
}

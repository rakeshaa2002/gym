import { SidebarProvider } from './context/useSidebarContext';
import { AuthProvider } from './context/AuthContext';
import AppRoutes from './routes/AppRoutes';
import { BrowserRouter } from 'react-router-dom';
import React from 'react';

function App() {
    return (
        <AuthProvider>
            <SidebarProvider>
                <BrowserRouter>
                    <AppRoutes />
                </BrowserRouter>
            </SidebarProvider>
        </AuthProvider>
    );
}

export default App;
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { IconLogout } from '@tabler/icons-react';
import { Dropdown, Form } from 'react-bootstrap';
import Flag from 'react-world-flags';
import SimpleBar from 'simplebar-react';
import { Link } from 'react-router-dom';
import { useSidebarContext } from '../context/useSidebarContext';

import logo from '/src/assets/images/logo/logo.png';
import adminimg from '/src/assets/images/avtar/profile.png';
import notification1 from '/src/assets/images/navnotification1.png';
import notification2 from '/src/assets/images/navnotification2.png';
import notification3 from '/src/assets/images/navnotification3.png';
import notification4 from '/src/assets/images/navnotification4.png';
import { IconBellRinging, IconChevronRight, IconLayout, IconLayoutGrid, IconMoon, IconSearch, IconSettings, IconSun, IconWorld } from '@tabler/icons-react';
export default function Header() {
    const navigate = useNavigate();
    const { logout } = useAuth(); 

    // Handle theme toggle
    const [theme, setTheme] = useState('light');
    const toggleTheme = () => {
        setTheme(prevTheme => prevTheme === 'dark' ? 'light' : 'dark');
    };

    // Apply theme classes
    useEffect(() => {
        document.body.setAttribute('data-bs-theme', theme);

    }, [theme]);

    const [navsearchData, setnavsearchData] = useState({
        navsearch: '',
    });
    const handleChange = (e) => {
        const { name, value } = e.target;
        setnavsearchData({ ...navsearchData, [name]: value, });
    };

    // Sidebar Action
    const { toggleSidebar } = useSidebarContext();

    // Logout Functionality
    const handleLogout = () => {
    console.log("Logout clicked");
    logout();
    localStorage.clear();
    console.log("✅ localStorage cleared");
    navigate('/sign-in');
    
    // Hard refresh to clear all state
    setTimeout(() => {
        window.location.href = '/sign-in';
    }, 500);
};
    


    return (
        <>
            <header className="codex-header">
                <div className="header-contian d-flex justify-content-between align-items-center">
                    <div className="header-left d-flex align-items-center">
                        <div className='codex-brand me-3'>
                            <Link className="d-flex align-items-center" to="/">
                                <img className="img-fluid" src={logo} alt="theeme-logo" />
                                <span className="fs-3 align-middle ms-2 lh-1">FitNexus</span>
                            </Link>
                        </div>
                        <div className="sidebar-action navicon-wrap me-3 d-xl-none" onClick={toggleSidebar}>
                            <IconLayoutGrid/>
                        </div>
                        <div className="input-group">
                            <span className="input-group-text pe-0">                               
                                <IconSearch/>
                            </span>
                            <Form.Control type="text" placeholder="Search Here" name="navsearch" value={navsearchData.navsearch} onChange={handleChange} />
                        </div>
                    </div>
                    <div className="header-right d-flex align-items-center justify-content-end">
                        <ul className="nav-iconlist">
                            <li>
                                <div className="navicon-wrap action-dark" onClick={toggleTheme}>
                                    {theme === 'dark' ?
                                        <IconSun/> : <IconMoon/>}
                                </div>
                            </li>
                            <li className="action-menu dropdown">
                                <Dropdown>
                                    <Dropdown.Toggle className="navicon-wrap notiicon-iconwrap">                                       
                                        <IconBellRinging/>
                                        <div className="noti-count"></div>
                                    </Dropdown.Toggle>
                                    <Dropdown.Menu className='action-dropdown navnotification-drop'>
                                        <div className="drop-header">
                                            <h5>
                                                notification<span className="float-end">05</span>
                                            </h5>
                                        </div>
                                        <SimpleBar>
                                            <ul>
                                                <li>
                                                    <Dropdown.Item href="#/action-1">
                                                        <div className="d-flex align-items-center">
                                                            <div className="icon-nav">
                                                                <img src={notification1} alt="" className="img-fluid" />
                                                            </div>
                                                            <div className="media-body">
                                                                <h6>Full Body Yoga</h6>
                                                                <span className="badge badge-success">08:30</span>
                                                            </div>
                                                        </div>                                                       
                                                        <IconChevronRight/>
                                                    </Dropdown.Item>
                                                </li>
                                                <li>
                                                    <Dropdown.Item href="#/action-2">
                                                        <div className="d-flex align-items-center">
                                                            <div className="icon-nav">
                                                                <img src={notification2} alt="" className="img-fluid" />
                                                            </div>
                                                            <div className="media-body">
                                                                <h6>Functional Workout</h6>
                                                                <span className="badge badge-success">08:30</span>
                                                            </div>
                                                        </div>
                                                        <IconChevronRight/>
                                                    </Dropdown.Item>
                                                </li>
                                                <li>
                                                    <Dropdown.Item href="#/action-3">
                                                        <div className="d-flex align-items-center">
                                                            <div className="icon-nav">
                                                                <img src={notification3} alt="" className="img-fluid" />
                                                            </div>
                                                            <div className="media-body">
                                                                <h6>Lower Body Express</h6>
                                                                <span className="badge badge-success">08:30</span>
                                                            </div>
                                                        </div>
                                                        <IconChevronRight/>
                                                    </Dropdown.Item>
                                                </li>
                                                <li>
                                                    <Dropdown.Item href="#/action-4">
                                                        <div className="d-flex align-items-center">
                                                            <div className="icon-nav">
                                                                <img src={notification4} alt="" className="img-fluid" />
                                                            </div>
                                                            <div className="media-body">
                                                                <h6>Glutes & Abs</h6>
                                                                <span className="badge badge-success">08:30</span>
                                                            </div>
                                                        </div>
                                                        <IconChevronRight/>
                                                    </Dropdown.Item>
                                                </li>
                                            </ul>
                                        </SimpleBar>
                                    </Dropdown.Menu>
                                </Dropdown>
                            </li>
                            <li className="action-menu dropdown">
                                <Dropdown>
                                    <Dropdown.Toggle className="navicon-wrap p-0">                                       
                                        <IconWorld/>
                                    </Dropdown.Toggle>
                                    <Dropdown.Menu className='action-dropdown navlang-drop'>
                                        <ul>
                                            <li>
                                                <Dropdown.Item href="#/action-1">
                                                    <Flag code="US" className='img-fluid' />
                                                    English
                                                </Dropdown.Item>
                                            </li>
                                            <li>
                                                <Dropdown.Item href="#/action-1">
                                                    <Flag code="DE" className='img-fluid' />
                                                    Deutsch
                                                </Dropdown.Item>
                                            </li>
                                            <li>
                                                <Dropdown.Item href="#/action-1">
                                                    <Flag code="ES" className='img-fluid' />
                                                    Español
                                                </Dropdown.Item>
                                            </li>
                                            <li>
                                                <Dropdown.Item href="#/action-1">
                                                    <Flag code="PT" className='img-fluid' />
                                                    Português
                                                </Dropdown.Item>
                                            </li>
                                        </ul>
                                    </Dropdown.Menu>
                                </Dropdown>
                            </li>
                            <li className="nav-profile action-menu dropdown">
                                <Dropdown>
                                    <Dropdown.Toggle className="user-icon action-toggle p-0">
                                        <img className="img-fluid" src={adminimg} alt="User Logo" />
                                    </Dropdown.Toggle>
                                    <Dropdown.Menu className="navprofile-drop action-dropdown">
                                        <ul>
                                            <li>
                                                <div className="media-body">
                                                    <img className="img-fluid rounded-circle" src={adminimg} alt="logo" />
                                                    <h6 className="mt-2 fw-bold">Hello Thomas</h6>
                                                </div>
                                            </li>
                                            <li>
                                                <Dropdown.Item as={Link} to="/profile">                                                   
                                                    <IconSettings className='me-2 align-middle'/>
                                                    setting
                                                </Dropdown.Item>
                                            </li>
                                            <li>
                                                <Dropdown.Item onClick={handleLogout}>                                                   
                                                    <IconLogout className='me-2 align-middle'/>
                                                    Logout
                                                </Dropdown.Item>
                                            </li>
                                        </ul>
                                    </Dropdown.Menu>
                                </Dropdown>
                            </li>
                        </ul>
                    </div>
                </div>
            </header>

        </>
    );
    
}
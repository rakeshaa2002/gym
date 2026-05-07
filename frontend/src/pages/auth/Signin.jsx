import React, { useState } from 'react'
import { Link } from "react-router-dom";
import { Container, Form, Row, Col, InputGroup, Alert } from 'react-bootstrap';
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { useAuth } from '../../context/AuthContext'; // Adjust path as needed

import logo from "/src/assets/images/logo/logo.png";
import welcomeimg from "/src/assets/images/welcome.png";
import google from "/src/assets/images/icon/icon-google.png";
import facebook from "/src/assets/images/icon/icon-facebook.png";
import apple from "/src/assets/images/icon/icon-apple.png";
import InputGroupText from 'react-bootstrap/esm/InputGroupText';
import { IconEye, IconEyeOff } from '@tabler/icons-react';

export default function Signin() {
    const [searchInpval, setsearchInpval] = useState({
        email: '',
        password: '',
        remember: ''
    });
    
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    
    const navigate = useNavigate();
    const { login } = useAuth(); // Get login function from context

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setsearchInpval({
            ...searchInpval,
            [name]: type === 'checkbox' ? checked : value
        });
        setError('');
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        if (!searchInpval.email || !searchInpval.password) {
            setError('Please enter both email and password');
            setLoading(false);
            return;
        }

        try {
            console.log("Attempting login with:", searchInpval.email);
            
            const response = await axios.post(
                "http://localhost:8080/api/auth/login",
                {
                    email: searchInpval.email,
                    password: searchInpval.password
                }
            );

            console.log("✅ Login Response:", response.data);

            // Handle both 'token' and 'accessToken' field names
            const tokenValue = response.data?.data?.token;

            if (tokenValue) {
                // Use auth context to update state
                login({
                    token: tokenValue,
                    accessToken: tokenValue,
                    email: response.data.data.email,
                    name: response.data.data.name,
                    role: response.data.data.role,
                    userId: response.data.data.userId
                });

                console.log("✅ Auth context updated with user data");
                console.log("User:", response.data.name, "Email:", response.data.email);

                // Wait 1 second then redirect (gives time for context to update)
                setTimeout(() => {
                    console.log("🚀 Navigating to home page (/)");
                    navigate("/");
                }, 1000);
            } else {
                setError("Login successful but no token received. Please contact support.");
            }

        } catch (error) {
            console.error("Login error:", error);

            if (error.response?.status === 401) {
                setError("Invalid email or password");
            } else if (error.response?.status === 404) {
                setError("User not found");
            } else if (error.code === 'ERR_NETWORK') {
                setError("Cannot connect to server. Make sure backend is running on localhost:8080");
            } else {
                setError(error.response?.data?.message || "Login failed. Please try again.");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            {/* Login Start */}
            <section className="py-lg-5 py-4 bg-white">
                <Container>
                    <Row className="align-items-center justify-content-center">
                        <Col md={6}>
                            <div className="codex-authbox px-lg-5">
                                <div className="codex-brand mb-lg-5 mb-4">
                                    <Link className="d-flex align-items-center justify-content-center" to="/" onClick={(e) => e.preventDefault()}>
                                        <img className="img-fluid" src={logo} alt="theeme-logo" />
                                        <span className="fs-3 align-middle ms-2 lh-1">FitNexus</span>
                                    </Link>
                                </div>
                                <h3>Welcome Back 👋</h3>
                                <p className="mb-lg-4 mb-2">Today is a new day. It's your day. You shape it. Sign in to start managing your projects.</p>

                                {error && (
                                    <Alert variant="danger" dismissible onClose={() => setError('')}>
                                        {error}
                                    </Alert>
                                )}

                                <Form onSubmit={handleSubmit}>
                                    <Row className="gy-4">
                                        <Col md="12">
                                            <Form.Label>Email</Form.Label>
                                            <Form.Control
                                                type="email"
                                                placeholder="example@gmail.com"
                                                name='email'
                                                value={searchInpval.email}
                                                onChange={handleChange}
                                                disabled={loading}
                                            />
                                        </Col>
                                        <Col md="12">
                                            <Form.Label>Password</Form.Label>
                                            <InputGroup>
                                                <Form.Control
                                                    type={showPassword ? "text" : "password"}
                                                    placeholder="At least 8 characters"
                                                    name='password'
                                                    value={searchInpval.password}
                                                    onChange={handleChange}
                                                    disabled={loading}
                                                />
                                                <InputGroupText
                                                    style={{ cursor: 'pointer' }}
                                                    onClick={() => setShowPassword(!showPassword)}
                                                >
                                                    {showPassword ? <IconEyeOff /> : <IconEye />}
                                                </InputGroupText>
                                            </InputGroup>
                                            <Link to="/forgot-password" className="float-end mt-3 d-inline-block text-primary">Forgot Password?</Link>
                                        </Col>
                                        <Col md="12">
                                            <Form.Check
                                                type="checkbox"
                                                label="Remember me"
                                                name='remember'
                                                checked={searchInpval.remember}
                                                onChange={handleChange}
                                                disabled={loading}
                                            />
                                        </Col>
                                        <Col md="12">
                                            <button
                                                type="submit"
                                                className="btn btn-primary w-100 py-3"
                                                disabled={loading}
                                            >
                                                {loading ? "Signing in..." : "Sign in"}
                                            </button>
                                        </Col>
                                    </Row>
                                </Form>

                                <div className="login-aur text-center py-4">
                                    <span className="bg-white">Or</span>
                                </div>
                                <ul className="login-with">
                                    <li>
                                        <Link to="#" onClick={(e) => e.preventDefault()} className="btn btn-light d-block">
                                            <img src={google} alt="" />
                                            Sign in with Google
                                        </Link>
                                    </li>
                                    <li>
                                        <Link to="#" onClick={(e) => e.preventDefault()} className="btn btn-light d-block">
                                            <img src={apple} alt="" />
                                            Sign in with Apple
                                        </Link>
                                    </li>
                                    <li>
                                        <Link to="#" onClick={(e) => e.preventDefault()} className="btn btn-light d-block">
                                            <img src={facebook} alt="" />
                                            Sign in with Facebook
                                        </Link>
                                    </li>
                                </ul>
                                <div className="text-center mt-4">
                                    Don't you have an account?
                                    <Link to="/sign-up" className="text-primary ms-2">Sign Up</Link>
                                </div>
                            </div>
                        </Col>
                        <Col lg={6} className="d-none d-lg-block">
                            <img src={welcomeimg} alt="" className="img-fluid" />
                        </Col>
                    </Row>
                </Container>
            </section>
            {/* Login End */}
        </>
    );
}
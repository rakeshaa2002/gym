import React, { useState } from 'react';
import { Row, Form, Container, Col } from 'react-bootstrap';
import * as TablerIcons from '@tabler/icons-react';
import { Link } from 'react-router-dom';
import logo from "/src/assets/images/logo/logo.png";

export default function Forgotpassword() {

    const [searchInpval, setsearchInpval] = useState({
        email: '',
    });
    const handleChange = (e) => {
        const { name, value } = e.target;
        setsearchInpval({ ...searchInpval, [name]: value, });
    };
    const handleSubmit = (e) => {
        e.preventDefault();
        console.log(searchInpval)
    };

    return (
        <>
            {/* Forgotpassword Start */}
            <section className="d-flex align-items-center vh-100">
                <Container>
                    <Row className="row align-items-center justify-content-center">
                        <Col md={5}>
                            <div className="codex-authbox p-lg-5 p-4 card">
                                <div className="codex-brand mb-lg-5 mb-4">
                                    <Link className="d-flex align-items-center justify-content-center" to="#" onClick={(e) => e.preventDefault()}>
                                        <img className="img-fluid" src={logo} alt="theeme-logo" />
                                        <span className="fs-3 align-middle ms-2 lh-1">FitNexus</span>
                                    </Link>
                                </div>
                                <h3>Forgot password ?</h3>
                                <p className="mb-lg-4 mb-2">Enter Your Email And We'll Send You A Link To Reset Your Password.</p>
                                <Form onSubmit={handleSubmit}>
                                    <Row className="gy-4">
                                        <Form.Group>
                                            <Form.Label>Email</Form.Label>
                                            <Form.Control type="password" placeholder="Enter your email" name='email' value={searchInpval.email} onChange={handleChange} />
                                        </Form.Group>
                                        <Form.Group>
                                            <button className="btn btn-primary w-100 py-3" type="submit">
                                                <TablerIcons.IconKey className='me-2' />
                                                Send Reset Link
                                            </button>
                                        </Form.Group>
                                    </Row>
                                </Form>
                            </div>
                        </Col>
                    </Row>
                </Container>
            </section>
            {/* Forgotpassword End */}
        </>
    )
}

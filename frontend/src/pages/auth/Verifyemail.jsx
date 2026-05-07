import { IconMail } from '@tabler/icons-react';
import React from 'react';
import { Row, Col, Container } from 'react-bootstrap';
export default function Verifyemail() {
    return (
        <>
            {/* veryf email start */}
            <section className='d-flex align-items-center vh-100'>
                <Container>
                    <Row className="align-items-center justify-content-center">
                        <Col md={5}>
                            <div className="codex-authbox p-lg-5 p-4 card text-center">
                                <div className="auth-header">
                                    <div className="auth-icon mb-2">
                                        <IconMail className='fs-3 text-primary'/>
                                    </div>
                                    <h3 className='mb-2'>Verify Your Email Address</h3>
                                    <p>you've entered <b>Crikho@Example.Com </b>as the email address for yiur account. please verify this email address by clicking button below.</p>
                                </div>
                                <div className="form-group mb-0 mt-3">
                                    <button className="btn btn-primary" type="submit">Verify Email</button>
                                </div>
                            </div>
                        </Col>
                    </Row>
                </Container>
            </section>
            {/* veryf email End*/}
        </>

    )
}

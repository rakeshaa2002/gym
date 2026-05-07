import { IconMailOpened } from '@tabler/icons-react';
import React, { useState } from 'react';
import { Row, Form,Container,Col } from 'react-bootstrap';
import { Link } from 'react-router-dom';
export default function Verifypin() {

    const [searchInpval, setsearchInpval] = useState({
        pinone: '',
        pintwo: '',
        pinthree: '',
        pintfour: '',
        pintfive: '',
        pintsix: '',
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

            {/* Verify pin start */}
            <section className='d-flex align-items-center vh-100'>
                <Container>
                    <Row className="align-items-center justify-content-center">
                        <Col md={5}>
                            <div className="codex-authbox p-lg-5 p-4 card text-center">
                                <div className="auth-header mb-2">
                                    <div className="auth-icon mb-2">                                                                              
                                        <IconMailOpened className='text-primary fs-3'/>
                                    </div>
                                    <h3 className='mb-2'>verify your email</h3>
                                    <p>
                                        Plase Enter The Verification Code We Sent
                                        <br /> To Crikho@example.Com
                                    </p>
                                </div>
                                <Form onSubmit={handleSubmit}>
                                    <Row className='3'>
                                        <Form.Group className="mb-0 gap-2 d-flex mb-4 ">
                                            <Form.Control className="code-input" type="number" name='pinone' value={searchInpval.pinone} onChange={handleChange} />
                                            <Form.Control className="code-input" type="number" name='pintwo' value={searchInpval.pintwo} onChange={handleChange} />
                                            <Form.Control className="code-input" type="number" name='pinthree' value={searchInpval.pinthree} onChange={handleChange} />
                                            <Form.Control className="code-input" type="number" name='pinfour' value={searchInpval.pinthree} onChange={handleChange} />
                                            <Form.Control className="code-input" type="number" name='pinfive' value={searchInpval.pintfour} onChange={handleChange} />
                                            <Form.Control className="code-input" type="number" name='pinsix' value={searchInpval.pintfive} onChange={handleChange} />
                                        </Form.Group>
                                        <Form.Group>
                                            <button className="btn btn-primary mt-0" type="submit">Confirm</button>
                                        </Form.Group>
                                    </Row>
                                </Form>
                                <div className="auth-footer text-center mt-2">
                                    <p>
                                        Dont Receive The Email ?
                                        <Link className="text-primary" href="#"> Resend Email</Link>
                                    </p>
                                </div>
                            </div>
                        </Col>
                    </Row>
                </Container>
            </section>
            {/* Verify pin End */}

        </>
    )
}

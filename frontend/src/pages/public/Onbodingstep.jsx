import React, { useState } from 'react';
import { Container, Row, Col, Tab, Nav, Form, Button } from 'react-bootstrap';
import DatePicker from "react-datepicker";
import Chart from "react-apexcharts";
import "react-datepicker/dist/react-datepicker.css";
import logo from "/src/assets/images/logo/logo.png";
import { onbodingchart } from '../js/Onboding';

// Onboarding Steps
import step1Img from '/src/assets/images/onboding/1.png';
import step2Img from '/src/assets/images/onboding/2.png';
import step3Img from '/src/assets/images/onboding/3.png';
import step4Img from '/src/assets/images/onboding/4.png';
import step5Img from '/src/assets/images/onboding/5.png';
import step6Img from '/src/assets/images/onboding/6.png';

// Gender
import womanImg from '/src/assets/images/onboding/woman.png';
import manImg from '/src/assets/images/onboding/man.png';
import neutralImg from '/src/assets/images/onboding/neautral.png';

// Goals
import loseWeightImg from '/src/assets/images/onboding/lose-weight.png';
import keepFitImg from '/src/assets/images/onboding/keep-fit.png';
import getStrongerImg from '/src/assets/images/onboding/get-stronger.png';
import gainMuscleImg from '/src/assets/images/onboding/gain-muscle.png';

// Activities
import cardioImg from '/src/assets/images/onboding/cardio.png';
import powerTrainingImg from '/src/assets/images/onboding/power-training.png';
import stretchImg from '/src/assets/images/onboding/stretch.png';
import dancingImg from '/src/assets/images/onboding/dancing.png';
import yogaImg from '/src/assets/images/onboding/yoga.png';
import { Link } from 'react-router-dom';
import { IconChevronLeft } from '@tabler/icons-react';

const stepsData = [
    { title: 'Choose gender', step: 'Step 1 of 6' },
    { title: 'Choose main goals!', step: 'Step 2 of 6' },
    { title: 'Select birth date', step: 'Step 3 of 6' },
    { title: 'How tall are you?', step: 'Step 4 of 6' },
    { title: 'Choose training level', step: 'Step 5 of 6' },
    { title: 'Choose activities that interest', step: 'Step 6 of 6' },
    { title: 'We create your training plan', step: null }
];

export default function Onbodingstep() {
    const [startDate, setStartDate] = useState(new Date());
    const [step, setStep] = useState(0);
    const nextStep = () => setStep((prev) => Math.min(prev + 1, stepsData.length - 1));
    const prevStep = () => setStep((prev) => Math.max(prev - 1, 0));

    const stepImages = [step1Img, step2Img, step3Img, step4Img, step5Img, step6Img];
    const genderImages = [womanImg, manImg, neutralImg];
    const goalImages = [loseWeightImg, keepFitImg, getStrongerImg, gainMuscleImg];
    const activityImages = [cardioImg, powerTrainingImg, stretchImg, dancingImg, yogaImg];

    return (
        <section className="d-flex align-items-center bg-light onbodying-main py-4 vh-100">
            <Container>
                <Row className="justify-content-center">
                    <Col md={10}>                   
                        <div className="form-step active">
                            <Row className="align-items-center justify-content-center">
                                {step < 6 && (
                                    <Col lg={6} className="d-none d-lg-block">
                                        <div className="onboding-imgwrap">
                                            <img src={stepImages[step]} alt="" className="img-fluid" />
                                        </div>
                                    </Col>
                                )}
                                <Col lg={5} md={8}>
                                    <div className="onboding-detail">
                                        <div className="codex-brand mb-4">
                                            <Link className="d-flex align-items-center justify-content-center" to="#" onClick={(e) => e.preventDefault()}>
                                                <img className="img-fluid" src={logo} alt="theme-logo" />
                                                <span className="fs-3 align-middle ms-2">FitNexus</span>
                                            </Link>
                                        </div>
                                        {step < 6 && (
                                            <div className="d-flex align-items-center justify-content-between mb-4">
                                                <span className="step-preve" onClick={prevStep}>                                                   
                                                    <IconChevronLeft className='fs-5 font-light'/>
                                                </span>
                                                <span className="font-light">{stepsData[step].step}</span>
                                                <span className="font-light step-skip" onClick={nextStep}>Skip</span>
                                            </div>
                                        )}
                                        <h3 className="mb-4 fw-bold text-center">{stepsData[step].title}</h3>
                                        {step === 0 && (
                                            <ul className="onboding-list">
                                                {[{ id: 'women', label: 'Woman', img: womanImg }, { id: 'man', label: 'Man', img: manImg }, { id: 'neautral', label: 'Neutral', img: neutralImg }].map(({ id, label, img }) => (
                                                    <li key={id}>
                                                        <input type="radio" name='gender' id={id} hidden />
                                                        <Form.Label htmlFor={id}>
                                                            <img src={img} alt={label} className="img-fluid" />
                                                            {label}
                                                        </Form.Label>
                                                    </li>
                                                ))}
                                            </ul>
                                        )}
                                        {step === 1 && (
                                            <ul className="onboding-list">
                                                {[{ id: 'loseweight', label: 'Lose Weight', img: loseWeightImg }, { id: 'keepfit', label: 'Keep Fit', img: keepFitImg }, { id: 'getstronger', label: 'Get Stronger', img: getStrongerImg }, { id: 'gainmass', label: 'Gain Muscle', img: gainMuscleImg }].map(({ id, label, img }) => (
                                                    <li key={id}>
                                                        <input type="radio" name='goal' id={id} hidden />
                                                        <Form.Label htmlFor={id}>
                                                            <img src={img} alt={label} className="img-fluid" />
                                                            {label}
                                                        </Form.Label>
                                                    </li>
                                                ))}
                                            </ul>
                                        )}

                                        {step === 2 && (
                                            <DatePicker className="form-control w-100" selected={startDate} onChange={(date) => setStartDate(date)} />
                                        )}

                                        {step === 3 && (
                                            <>
                                                <Tab.Container defaultActiveKey="feet">
                                                    <Nav variant="tabs">
                                                        <Nav.Item>
                                                            <Nav.Link eventKey="feet">Feet</Nav.Link>
                                                        </Nav.Item>
                                                        <Nav.Item>
                                                            <Nav.Link eventKey="cm">Centimeter</Nav.Link>
                                                        </Nav.Item>
                                                    </Nav>

                                                    <Tab.Content className="mt-3">
                                                        <Tab.Pane eventKey="feet">
                                                            <Row className="justify-content-center">
                                                                <Col md={4}>
                                                                    <div className="d-flex align-items-center gap-2">
                                                                        <Form.Control type="text" />
                                                                        <span>feet</span>
                                                                    </div>
                                                                </Col>
                                                            </Row>
                                                        </Tab.Pane>

                                                        <Tab.Pane eventKey="cm">
                                                            <Row className="justify-content-center">
                                                                <Col md={4}>
                                                                    <div className="d-flex align-items-center gap-2">
                                                                        <Form.Control type="text" />
                                                                        <span>cm</span>
                                                                    </div>
                                                                </Col>
                                                            </Row>
                                                        </Tab.Pane>
                                                    </Tab.Content>
                                                </Tab.Container>
                                            </>
                                        )}

                                        {step === 4 && (
                                            <ul className="onboding-list">
                                                {["beginner", "irregulartraining", "medium", "advanced"].map((level) => (
                                                    <li key={level}>                                                       
                                                        <input type="radio" name="training" id={level} hidden />
                                                        <Form.Label htmlFor={level}>
                                                            {level.charAt(0).toUpperCase() + level.slice(1)}
                                                            <span className="fs-6 font-light d-block">
                                                                {level === 'beginner' ? 'I want to start training' :
                                                                    level === 'irregulartraining' ? 'I train 1-2 times a week' :
                                                                        level === 'medium' ? 'I train 3-5 times a week' :
                                                                            'I train more than 5 times a week'}
                                                            </span>
                                                        </Form.Label>
                                                    </li>
                                                ))}
                                            </ul>
                                        )}

                                        {step === 5 && (
                                            <ul className="onboding-list">
                                                {[{ id: 'cardio', label: 'Cardio', img: cardioImg }, { id: 'power', label: 'Power', img: powerTrainingImg }, { id: 'stretch', label: 'Stretch', img: stretchImg }, { id: 'dancing', label: 'Dancing', img: dancingImg }, { id: 'yoga', label: 'Yoga', img: yogaImg }].map(({ id, label, img }) => (
                                                    <li key={id}>
                                                        <input type="radio" name="activites" id={id} hidden />
                                                        <Form.Label htmlFor={id}>
                                                            <img src={img} alt={label} className="img-fluid" />
                                                            {label}
                                                        </Form.Label>
                                                    </li>
                                                ))}
                                            </ul>
                                        )}

                                        {step === 6 && (
                                            <div className="createtraining-card text-center">                                               
                                                <Chart options={onbodingchart} series={onbodingchart.series} height={305} type='radialBar' className="mt-3" />
                                                <p>We create a workout according to demographic profile, activity level and interests</p>
                                            </div>
                                        )}

                                        <Button className="btn btn-primary btn-lg py-3 mt-5 w-100 btn-continue" onClick={nextStep}>
                                            {step === 1 || step === 4 || step === 6 ? 'Start Training' : 'Continue'}
                                        </Button>
                                    </div>
                                </Col>
                            </Row>
                        </div>
                    </Col>
                </Row>
            </Container>
        </section>
    );
}

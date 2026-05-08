import React, { useState, useEffect } from 'react';
import { Link } from "react-router-dom";
import { Row, Col, Card, Table, Container, Form, CardBody, ProgressBar, Button } from 'react-bootstrap';
import Chart from "react-apexcharts";
import ReactECharts from "echarts-for-react";
import Slider from "react-slick";
import Footer from '../../components/Footer';
import SimpleBar from 'simplebar-react';
import { progresschart, activitychart, heartrate, calories, waterGaugeOptions } from '../js/Dashboard1';
import running from "/src/assets/images/dashboard/running.png"
import sleeping from "/src/assets/images/dashboard/sleeping.png"
import weightlifting from "/src/assets/images/dashboard/weightlifting.png"
import weightloss from "/src/assets/images/dashboard/wightloss.png"
import boxing from "/src/assets/images/dashboard/boxing.png"
import cardio from "/src/assets/images/dashboard/cardio.png"
import weight from "/src/assets/images/dashboard/weight.png"
import stretching from "/src/assets/images/dashboard/stretching.png"
import arn from "/src/assets/images/dashboard/arm.png"
import yoga from "/src/assets/images/dashboard/yoga.png"
import workou1 from "/src/assets/images/dashboard/workout1.png"
import workou2 from "/src/assets/images/dashboard/workout2.png"
import workou3 from "/src/assets/images/dashboard/workout3.png"
import stepCardBg from "/src/assets/images/dashboard/shap-partern.png"
import trainingBg from "/src/assets/images/dashboard/training-bg.png"
import sumosquate from "/src/assets/images/workout/sumo-squat.png"
import frontlunge from "/src/assets/images/workout/front-lunge.png"
import legabductaion from "../../assets/images/workout/leg-abduction.png"
import armcircles from "../../assets/images/workout/arm-circles.png"
import { IconArrowUpRight, IconDroplet, IconGuitarPick, IconHeartbeat, IconHeartFilled, IconInfoCircle, IconPoint, IconRun, IconSun } from '@tabler/icons-react';

export default function Index() {
    var cardslider = {
        dots: false,
        infinite: true,       
        speed: 500,
        slidesToShow: 3,
        slidesToScroll: 1,
        autoplay: true,
        autoplaySpeed: 3000,
        responsive: [
            { breakpoint: 1441, settings: { slidesToShow: 3 } },
            { breakpoint: 768, settings: { slidesToShow: 2 } },
            { breakpoint: 481, settings: { slidesToShow: 1 } }
        ]
    };
    const [formData, setFormData] = useState({
        quickTransfer: '',
        enterAmount: '',
    });
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value, });
    };
    const handleSubmit = (e) => {
        e.preventDefault();
    };

    const [waterValue, setWaterValue] = useState(2.25);
    const maxValue = 3;

    // Get chart options using current waterValue
    const options = waterGaugeOptions(waterValue, maxValue);

    return (
        <>

            {/* Theme Body Start */}
            <main className="themebody-wrap">

                <div className="theme-body">
                    <Container fluid>
                        <Row>
                            {/* Step Card */}
                            <Col lg={3} md={6}>
                                        <Card className="bg-primary text-white step-card" style={{ backgroundImage: `url(${stepCardBg})` }}>
                                    <Card.Body className="p-3">
                                        <h6 className="mb-4">                                                                                      
                                            <IconRun className='me-1'/>
                                            Steps
                                        </h6>
                                        <h2 className="mb-4 fw-semibold">
                                            3.500 <small>Steps</small>
                                        </h2>
                                        <ProgressBar now={25} />
                                        <p className="mt-3 text-white">50% of your goals</p>
                                    </Card.Body>
                                </Card>
                            </Col>

                            {/* Water */}
                            <Col lg={3} md={6}>
                                <Card className="bg-secondary text-white step-card">
                                    <Card.Body className="p-3 pb-0">
                                        <h6>                                           
                                            <IconDroplet className='me-1'/>
                                            Water
                                        </h6>
                                        <ReactECharts option={options} style={{ height: 150 }} />
                                    </Card.Body>
                                </Card>
                            </Col>
                            <Col lg={3} md={6}>
                                <Card className="bg-info text-white step-card">
                                    <Card.Body className="p-3 pb-0">
                                        <h6>                                           
                                            <IconGuitarPick className='me-1'/>
                                            Calories
                                        </h6>
                                        <ReactECharts option={calories} style={{ width: '100%', height: '150px' }} opts={{ renderer: "svg" }} />
                                    </Card.Body>
                                </Card>
                            </Col>
                            <Col lg={3} md={6}>
                                <Card className="bg-danger text-white step-card">
                                    <Card.Body className="p-3">
                                        <h6>                                           
                                            <IconHeartbeat className='me-1'/>
                                            Heart Rate
                                        </h6>
                                        <ReactECharts option={heartrate} style={{ width: "100%", height: "135px" }} opts={{ renderer: "svg" }} />
                                    </Card.Body>
                                </Card>
                            </Col>
                            <Col xxl={7}>
                                <Row>
                                    <Col md={6}>
                                        <Card>
                                            <Card.Header>
                                                <h4>Activity</h4>
                                                <select>
                                                    <option>Weekly</option>
                                                    <option>Monthly</option>
                                                    <option>Yearly</option>
                                                </select>
                                            </Card.Header>
                                            <Card.Body className="pt-0">
                                                <Chart options={activitychart} series={activitychart.series} height={270} type='bar' className="mt-3" />
                                            </Card.Body>
                                        </Card>
                                    </Col>
                                    <Col md={6}>
                                        <Card className="progress-card">
                                            <Card.Header>
                                                <h4>Progress</h4>
                                                <select>
                                                    <option>Weekly</option>
                                                    <option>Monthly</option>
                                                    <option>Yearly</option>
                                                </select>
                                            </Card.Header>
                                            <Card.Body className="pt-0">
                                                <Chart options={progresschart} series={progresschart.series} height={305} type='donut' className="mt-3" />
                                            </Card.Body>
                                        </Card>
                                    </Col>
                                    <Col md={6}>
                                        <Card className="goals-card">
                                            <Card.Body className="d-flex justify-content-between">
                                                <div className="d-flex align-items-center">
                                                    <div className="icon-wrap me-3">
                                                        <img src={running} className='img-fluid' />
                                                    </div>
                                                    <div>
                                                        <h6 className="fw-semibold">Running</h6>
                                                        <p>70km/80km</p>
                                                    </div>
                                                </div>
                                                <div id="runnin-chart" className="goal-chart"></div>
                                            </Card.Body>
                                        </Card>
                                    </Col>

                                    <Col md={6}>
                                        <Card className="goals-card">
                                            <Card.Body className="d-flex justify-content-between">
                                                <div className="d-flex align-items-center">
                                                    <div className="icon-wrap me-3">
                                                        <img src={sleeping} className='img-fluid' />
                                                    </div>
                                                    <div>
                                                        <h6 className="fw-semibold">Sleeping</h6>
                                                        <p>50hrs/60hrs</p>
                                                    </div>
                                                </div>
                                                <div id="sleeping-chart" className="goal-chart"></div>
                                            </Card.Body>
                                        </Card>
                                    </Col>

                                    <Col md={6}>
                                        <Card className="goals-card">
                                            <Card.Body className="d-flex justify-content-between">
                                                <div className="d-flex align-items-center">
                                                    <div className="icon-wrap me-3">
                                                        <img src={weightlifting} className='img-fluid' />
                                                    </div>
                                                    <div>
                                                        <h6 className="fw-semibold">Weight Lifting</h6>
                                                        <p>4/10 Sets</p>
                                                    </div>
                                                </div>
                                                <div id="weightlifting-chart" className="goal-chart"></div>
                                            </Card.Body>
                                        </Card>
                                    </Col>

                                    <Col md={6}>
                                        <Card className="goals-card">
                                            <Card.Body className="d-flex justify-content-between">
                                                <div className="d-flex align-items-center">
                                                    <div className="icon-wrap me-3">
                                                        <img src={weightloss} className='img-fluid' />
                                                    </div>
                                                    <div>
                                                        <h6 className="fw-semibold">Weight Loss</h6>
                                                        <p>70kg/100kg</p>
                                                    </div>
                                                </div>
                                                <div id="weightloss-chart" className="goal-chart"></div>
                                            </Card.Body>
                                        </Card>
                                    </Col>
                                    <Col md={12}>
                                        <Card>
                                            <Card.Header className="d-flex justify-content-between">
                                                <h4>Popular Workouts</h4>
                                                <Link to="/workout-plan" className="text-primary fs-6 fw-bold">
                                                    See more                                                    
                                                    <IconArrowUpRight className='fs-4 lh-1 align-middle'/>
                                                </Link>
                                            </Card.Header>
                                            <Card.Body>
                                                <Slider {...cardslider} className="popularworkout-slider arrow-style1">
                                                    <div>
                                                        <div className="workout-grid">
                                                            <div className="img-wrap">
                                                                <Link to="#" onClick={(e) => e.preventDefault()} className="btn-wishlist">                                                                   
                                                                    <IconHeartFilled/>                                      
                                                                </Link>
                                                                <Link to="/workout-detail">
                                                                    <img src={workou1} alt="" className="w-100" />
                                                                </Link>
                                                            </div>
                                                            <div className="workout-detail">
                                                                <Link to="/workout-detail">
                                                                    <h5 className="fw-semibold mb-2">Piriformis Stretch</h5>
                                                                </Link>
                                                                <h6 className="fw-semibold">
                                                                    Beginner
                                                                    <span>                                                                       
                                                                        <IconPoint/>
                                                                        20 sec
                                                                    </span>
                                                                </h6>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div>
                                                        <div className="workout-grid">
                                                            <div className="img-wrap">
                                                                <Link to="#" onClick={(e) => e.preventDefault()} className="btn-wishlist">
                                                                    <IconHeartFilled/>                                                                   
                                                                </Link>
                                                                <Link to="/workout-detail">
                                                                    <img src={workou3} alt="" className="w-100" />
                                                                </Link>
                                                            </div>
                                                            <div className="workout-detail">
                                                                <Link to="/workout-detail">
                                                                    <h5 className="fw-semibold mb-2">Frankensteins</h5>
                                                                </Link>
                                                                <h6 className="fw-semibold">
                                                                    Beginner
                                                                    <span>                                                                       
                                                                        <IconPoint/>
                                                                        10 min
                                                                    </span>
                                                                </h6>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div>
                                                        <div className="workout-grid">
                                                            <div className="img-wrap">
                                                                <Link to="#" onClick={(e) => e.preventDefault()} className="btn-wishlist">
                                                                    <IconHeartFilled/>
                                                                </Link>
                                                                <Link to="/upperbody-workout">
                                                                    <img src={workou3} alt="" className="w-100" />
                                                                </Link>
                                                            </div>
                                                            <div className="workout-detail">
                                                                <Link to="/upperbody-workout">
                                                                    <h5 className="fw-semibold mb-2">Rapid Lower Body</h5>
                                                                </Link>
                                                                <h6 className="fw-semibold">
                                                                    Beginner
                                                                    <span>
                                                                        <IconPoint/>
                                                                        30 min
                                                                    </span>
                                                                </h6>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div>
                                                        <div className="workout-grid">
                                                            <div className="img-wrap">
                                                                <Link to="#" onClick={(e) => e.preventDefault()} className="btn-wishlist">
                                                                    <IconHeartFilled/>
                                                                </Link>
                                                                <Link to="/upperbody-workout">
                                                                    <img src={workou1} alt="" className="w-100" />
                                                                </Link>
                                                            </div>
                                                            <div className="workout-detail">
                                                                <Link to="/upperbody-workout">
                                                                    <h5 className="fw-semibold mb-2">Piriformis Stretch</h5>
                                                                </Link>
                                                                <h6 className="fw-semibold">
                                                                    Beginner
                                                                    <span>
                                                                        <IconPoint/>
                                                                        20 sec
                                                                    </span>
                                                                </h6>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </Slider>
                                            </Card.Body>
                                        </Card>
                                    </Col>
                                </Row>
                            </Col>
                            <Col xxl={5}>
                                <Row>
                                    <Col xxl={12} lg={6}>
                                        <Card className="training-card training-card" style={{ backgroundImage: `url(${trainingBg})` }}>
                                            <Card.Body>
                                                <Row>
                                                    <Col md={8}>
                                                        <h3 className="mb-2 fw-bold">Full Body Toning Workout</h3>
                                                        <p>Incircuits circuits to work every muscle</p>                                                       
                                                        <Link to="/onboding-step" className="btn btn-primary mt-4">Start Training</Link>
                                                    </Col>
                                                </Row>
                                            </Card.Body>
                                        </Card>
                                    </Col>
                                    <Col xxl={12} lg={6}>
                                        <Card>
                                            <Card.Header className="d-flex justify-content-between">
                                                <h4 className="fw-semibold">Categories</h4>
                                                <Link to="/workout-plan" className="text-primary fs-6 fw-bold">
                                                    See more 
                                                    <IconArrowUpRight className='fs-4 lh-1 align-middle'/>                                                   
                                                </Link>
                                            </Card.Header>
                                            <Card.Body>
                                                <ul className="category-list">
                                                    <li><Link to="#"><img src={boxing} className='img-fluid' /> Boxing</Link></li>
                                                    <li><Link to="#"><img src={cardio} className='img-fluid' /> Cardio</Link></li>
                                                    <li><Link to="#"><img src={weight} className='img-fluid' /> Gym</Link></li>
                                                    <li><Link to="#"><img src={stretching} className='img-fluid' /> Stretch</Link></li>
                                                    <li><Link to="#"><img src={arn} className='img-fluid' /> Upper Body</Link></li>
                                                    <li><Link to="#"><img src={yoga} className='img-fluid' /> Yoga</Link></li>
                                                </ul>
                                            </Card.Body>
                                        </Card>
                                    </Col>
                                    <Col xxl={12} lg={6}>
                                        <Card>
                                            <Card.Body>
                                                <div className="d-flex align-items-center justify-content-between mb-3">
                                                    <div>
                                                        <h4 className="fw-semibold">Best Exercises</h4>
                                                        <span className="font-light">Exercises: 210</span>
                                                    </div>
                                                    <Button variant="primary">All Exercises</Button>
                                                </div>
                                                <ul className="warmup-list">
                                                    <li>
                                                        <div className="d-flex align-items-center justify-content-between">
                                                            <div className="d-flex align-items-center">
                                                                <div className="img-wrap">
                                                                    <img src={sumosquate} alt="" className="img-fluid" />
                                                                </div>
                                                                <div>
                                                                    <h6 className="fw-semibold">Sumo Squat</h6>
                                                                    <p className="font-light">08:40 - 09:15</p>
                                                                </div>
                                                            </div>
                                                            <Link to="#" onClick={(e) => e.preventDefault()} className="fs-5" data-bs-toggle="modal" data-bs-target="#exampleModal">                                                               
                                                                <IconInfoCircle/>
                                                            </Link>
                                                        </div>
                                                    </li>
                                                    <li>
                                                        <div className="d-flex align-items-center justify-content-between">
                                                            <div className="d-flex align-items-center">
                                                                <div className="img-wrap">
                                                                    <img src={frontlunge} alt="" className="img-fluid" />
                                                                </div>
                                                                <div>
                                                                    <h6 className="fw-semibold">Front Lunge</h6>
                                                                    <p className="font-light">08:40 - 09:15</p>
                                                                </div>
                                                            </div>
                                                            <Link to="#" onClick={(e) => e.preventDefault()} className="fs-5" data-bs-toggle="modal" data-bs-target="#exampleModal">
                                                                <IconInfoCircle/>
                                                            </Link>
                                                        </div>
                                                    </li>
                                                    <li>
                                                        <div className="d-flex align-items-center justify-content-between">
                                                            <div className="d-flex align-items-center">
                                                                <div className="img-wrap">
                                                                    <img src={legabductaion} alt="" className="img-fluid" />
                                                                </div>
                                                                <div>
                                                                    <h6 className="fw-semibold">Leg Abduction</h6>
                                                                    <p className="font-light">08:40 - 09:15</p>
                                                                </div>
                                                            </div>
                                                            <Link to="#" onClick={(e) => e.preventDefault()} className="fs-5" data-bs-toggle="modal" data-bs-target="#exampleModal">
                                                                <IconInfoCircle/>
                                                            </Link>
                                                        </div>
                                                    </li>
                                                    <li>
                                                        <div className="d-flex align-items-center justify-content-between">
                                                            <div className="d-flex align-items-center">
                                                                <div className="img-wrap">
                                                                    <img src={armcircles} alt="" className="img-fluid" />
                                                                </div>
                                                                <div>
                                                                    <h6 className="fw-semibold">Arm circles</h6>
                                                                    <p className="font-light">08:40 - 09:15</p>
                                                                </div>
                                                            </div>
                                                            <Link to="#" onClick={(e) => e.preventDefault()} className="fs-5" data-bs-toggle="modal" data-bs-target="#exampleModal">
                                                                <IconInfoCircle/>
                                                            </Link>
                                                        </div>
                                                    </li>
                                                </ul>
                                            </Card.Body>
                                        </Card>
                                    </Col>
                                </Row>
                            </Col>
                        </Row>
                    </Container>
                </div>
            </main>
            {/* Theme Body End */}

            {/* Footer Start */}
            <Footer />
            {/* Footer End */}

        </>
    );
}

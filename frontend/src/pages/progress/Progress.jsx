import React, { useState, useEffect } from 'react';
import { Link } from "react-router-dom";
import { Row, Col, Card, Table, Container, Form, CardBody, Modal, Button } from 'react-bootstrap';
import Chart from "react-apexcharts";
import { workoutgoal, heartbeat, weightchart, workoutactivity, caloriesstatistic } from '../js/Progress';
import Footer from '../../components/Footer';
import { IconDots, IconHeart, IconSearch } from '@tabler/icons-react';

export default function Progress() {

    return (
        <>

            {/* Theme Body Start */}
            <main className="themebody-wrap">
                <div className="theme-body">
                    <Container fluid>
                        <Row>
                            <Col md={12}>
                                <Card>
                                    <CardBody>
                                        <Row className="gy-3 mb-4">
                                            <Col xxl={8}>
                                                <div className="d-sm-flex d-inline-grid align-items-center gap-3">
                                                    <h5>Workout Time</h5>
                                                    <div className="d-flex align-items-center gap-3 p-3 rounded-3 bg-light ">
                                                        <h4><strong className="fw-bold">12</strong> <small className="font-light">Hours</small></h4>
                                                        <h4><strong className="fw-bold">35</strong> <small className="font-light">Minutes</small></h4>
                                                    </div>
                                                    <h5>Total Workout</h5>
                                                    <div className="d-flex align-items-center gap-3 p-3 rounded-3 bg-light ">
                                                        <h4><strong className="fw-bold">14</strong> <small className="font-light">Exercises</small></h4>
                                                    </div>
                                                </div>
                                            </Col>
                                            <Col xxl={4} md={6}>
                                                <div className="d-flex align-items-center justify-content-between gap-4">
                                                    <div className="input-group">
                                                        <span className="input-group-text pe-0">                                                           
                                                            <IconSearch/>
                                                        </span>
                                                        <input type="text" placeholder="Search for menu" className="form-control" />
                                                    </div>
                                                    <Link to="#" onClick={(e) => e.preventDefault()} className="btn btn-primary text-nowrap">This Week</Link>
                                                </div>
                                            </Col>
                                        </Row>
                                        <Row>
                                            <Col xxl="6">
                                                <Row>
                                                    <Col md={6}>
                                                        <Card className="shadow-none bg-light-secondary">
                                                            <CardBody>
                                                                <div className="d-flex align-items-center justify-content-between">
                                                                    <h4>                                                                       
                                                                        <IconHeart className='me-1'/>
                                                                        Health Score
                                                                    </h4>
                                                                    <Link to="#" onClick={(e) => e.preventDefault()}>                                                                       
                                                                        <IconDots className='fs-4'/>
                                                                    </Link>
                                                                </div>
                                                                <div className="d-flex align-items-center mt-4">
                                                                    <h2>82%</h2><span className="badge badge-light ms-2">Very Healthy</span>
                                                                </div>
                                                                <div className="progress my-2" style={{ height: "8px" }}>
                                                                    <div className="progress-bar bg-secondary" style={{ width: "60%" }}></div>
                                                                </div>
                                                                <span>Keep up your good work, Kalendra!</span>
                                                            </CardBody>
                                                        </Card>
                                                        <Card className="shadow-none bg-light-danger">
                                                            <CardBody>
                                                                <div className="d-flex align-items-center justify-content-between">
                                                                    <h4>
                                                                        <IconHeart className='me-1'/>
                                                                        Heart Best
                                                                    </h4>
                                                                                              <Link to="#" onClick={(e) => e.preventDefault()}>
                                                                        <IconDots className='fs-4'/>
                                                                    </Link>
                                                                </div>
                                                                <div className="d-flex align-items-center mt-4">
                                                                    <h2>110 <small className="fs-5">bpm</small></h2><span className="badge badge-light ms-2">Normal</span>
                                                                </div>
                                                                <span className="mt-2 d-block">You are calm and ready for exercises!</span>
                                                                <Chart options={heartbeat} series={heartbeat.series} height={20} type='line' className="mt-3" />
                                                            </CardBody>
                                                        </Card>
                                                        <Card className="shadow-none border">
                                                            <CardBody>
                                                                <div className="d-flex align-items-center justify-content-between">
                                                                    <h4>
                                                                        Weight Data
                                                                    </h4>
                                                                                              <Link to="#" onClick={(e) => e.preventDefault()}>
                                                                        <IconDots className='fs-4'/>
                                                                    </Link>
                                                                </div>
                                                                <Table className="mt-2 mb-0">
                                                                    <tbody>
                                                                        <tr>
                                                                            <td className="px-0 align-middle">Current Weight</td>
                                                                            <td className="px-0 align-middle text-end">
                                                                                <span className="fs-5">72</span>
                                                                                <span className="font-light">kg</span>
                                                                            </td>
                                                                        </tr>
                                                                        <tr>
                                                                            <td className="px-0 align-middle">Weight Goal</td>
                                                                            <td className="px-0 align-middle text-end">
                                                                                <span className="fs-5">65</span>
                                                                                <span className="font-light">kg</span>
                                                                            </td>
                                                                        </tr>
                                                                        <tr>
                                                                            <td className="px-0 align-middle">Progress</td>
                                                                            <td className="px-0 align-middle text-end">
                                                                                <span className="fs-5">18</span>
                                                                                <span className="font-light">%</span>
                                                                            </td>
                                                                        </tr>
                                                                    </tbody>
                                                                </Table>
                                                                <Chart options={weightchart} series={weightchart.series} height={320} type='area' className="mt-3" />
                                                            </CardBody>
                                                        </Card>
                                                    </Col>
                                                    <Col md={6}>
                                                        <Card className="shadow-none bg-light-info">
                                                            <CardBody>
                                                                <div className="d-flex align-items-center justify-content-between">
                                                                    <h4>
                                                                        <IconHeart className='me-1'/>
                                                                        Workout Goals
                                                                    </h4>
                                                                                              <Link to="#" onClick={(e) => e.preventDefault()}>
                                                                        <IconDots className='fs-4'/>
                                                                    </Link>
                                                                </div>
                                                                <Chart options={workoutgoal} series={workoutgoal.series} height={260} type='radialBar' />
                                                                <span className="text-center d-block">Almost there! Keep Pushing to reach <br /> your goal!</span>
                                                            </CardBody>
                                                        </Card>
                                                        <div>
                                                            <h4 className="mb-2 fw-semibold">Goals List</h4>
                                                            <Card className="shadow-none border">
                                                                <CardBody>
                                                                    <h5 className="fw-semibold mb-2">Complete 5K Runs</h5>
                                                                    <span className="badge badge-light">Running</span>
                                                                    <span className="font-light">25 km (5 runs of 5 km each)</span>
                                                                    <div className="mt-3">
                                                                        <div className="d-flex justify-content-between align-items-center mb-2">
                                                                            <h4 className="fw-semibold">60%</h4>
                                                                            <span className="font-light">15/25 km</span>
                                                                        </div>
                                                                        <div className="progress" style={{ height: "10px" }}>
                                                                            <div className="progress-bar bg-primary" style={{ width: "60%" }}></div>
                                                                        </div>
                                                                    </div>
                                                                </CardBody>
                                                            </Card>
                                                            <Card className="shadow-none border">
                                                                <CardBody>
                                                                    <h5 className="fw-semibold mb-2">Weekly Yoga Practice</h5>
                                                                    <span className="badge badge-light">Yoga</span>
                                                                    <span className="font-light">4 sessions per week</span>
                                                                    <div className="mt-3">
                                                                        <div className="d-flex justify-content-between align-items-center mb-2">
                                                                            <h4 className="fw-semibold">75%</h4>
                                                                            <span className="font-light">3/4 sessions</span>
                                                                        </div>
                                                                        <div className="progress" style={{ height: "10px" }}>
                                                                            <div className="progress-bar bg-primary" style={{ width: "75%" }}></div>
                                                                        </div>
                                                                    </div>
                                                                </CardBody>
                                                            </Card>
                                                            <Card className="shadow-none border">
                                                                <CardBody>
                                                                    <h5 className="fw-semibold mb-2">Daily Step Count</h5>
                                                                    <span className="badge badge-light">Walking</span>
                                                                    <span className="font-light">70,000 steps/week</span>
                                                                    <div className="mt-3">
                                                                        <div className="d-flex justify-content-between align-items-center mb-2">
                                                                            <h4 className="fw-semibold">85%</h4>
                                                                            <span className="font-light">59,500/70,000 steps</span>
                                                                        </div>
                                                                        <div className="progress" style={{ height: "10px" }}>
                                                                            <div className="progress-bar bg-primary" style={{ width: "85%" }}></div>
                                                                        </div>
                                                                    </div>
                                                                </CardBody>
                                                            </Card>
                                                        </div>
                                                    </Col>
                                                </Row>
                                            </Col>
                                            <Col xxl={6}>
                                                <Card className="border shadow-none">
                                                    <CardBody>
                                                        <div className="d-flex justify-content-between align-items-center">
                                                            <h4 className="fw-bold">Workout Activity</h4>
                                                            <Link to="#" onClick={(e) => e.preventDefault()}>                                                               
                                                                <IconDots className='fs-4'/>
                                                            </Link>
                                                        </div>
                                                        <Chart options={workoutactivity} series={workoutactivity.series} height={255} type='bar' />
                                                    </CardBody>
                                                </Card>
                                                <Card className="border shadow-none">
                                                    <CardBody>
                                                        <div className="d-flex justify-content-between align-items-center">
                                                            <h4 className="fw-bold">Calories Statistic</h4>
                                                            <Link to="#" onClick={(e) => e.preventDefault()}>                                                               
                                                                <IconDots className='fs-4'/>
                                                            </Link>
                                                        </div>
                                                        <Chart options={caloriesstatistic} series={caloriesstatistic.series} height={280} type='bar' />
                                                        <Row className="gy-4">
                                                            <Col md={6}>
                                                                <Card className="bg-light-primary mb-0">
                                                                    <CardBody className="p-3">
                                                                        <h4 className="mb-3">
                                                                            Calories
                                                                        </h4>
                                                                        <div>
                                                                            <div className="progress" style={{ height: "10px" }}>
                                                                                <div className="progress-bar bg-primary" style={{ width: "25%" }}></div>
                                                                            </div>
                                                                            <h6 className="mt-3">1,750/ <span className="font-light">2,500 cal</span></h6>
                                                                        </div>
                                                                    </CardBody>
                                                                </Card>
                                                            </Col>
                                                            <Col md={6}>
                                                                <Card className="bg-light-secondary mb-0">
                                                                    <CardBody className="p-3">
                                                                        <h4 className="mb-3">
                                                                            Protein
                                                                        </h4>
                                                                        <div>
                                                                            <div className="progress" style={{ height: "10px" }}>
                                                                                <div className="progress-bar bg-secondary" style={{ width: "25%" }}></div>
                                                                            </div>
                                                                            <h6 className="mt-3">25/ <span className="font-light">32 gr</span></h6>
                                                                        </div>
                                                                    </CardBody>
                                                                </Card>
                                                            </Col>
                                                            <Col md={6}>
                                                                <Card className="bg-light-success mb-0">
                                                                    <CardBody className="p-3">
                                                                        <h4 className="mb-3">
                                                                            Carbs
                                                                        </h4>
                                                                        <div>
                                                                            <div className="progress" style={{ height: "10px" }}>
                                                                                <div className="progress-bar bg-success" style={{ width: "25%" }}></div>
                                                                            </div>
                                                                            <h6 className="mt-3">67/ <span className="font-light">120 gr</span></h6>
                                                                        </div>
                                                                    </CardBody>
                                                                </Card>
                                                            </Col>
                                                            <Col md={6}>
                                                                <Card className="bg-light-info mb-0">
                                                                    <CardBody className="p-3">
                                                                        <h4 className="mb-3">
                                                                            Fats
                                                                        </h4>
                                                                        <div>
                                                                            <div className="progress" style={{ height: "10px" }}>
                                                                                <div className="progress-bar bg-info" style={{ width: "25%" }}></div>
                                                                            </div>
                                                                            <h6 className="mt-3">42/ <span className="font-light">48 gr</span></h6>
                                                                        </div>
                                                                    </CardBody>
                                                                </Card>
                                                            </Col>
                                                        </Row>
                                                    </CardBody>
                                                </Card>
                                            </Col>
                                        </Row>
                                    </CardBody>
                                </Card>
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
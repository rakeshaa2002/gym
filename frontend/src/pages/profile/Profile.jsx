import React, { useState } from "react";
import { Link } from "react-router-dom";
import { Row, Col, Card, Table, Container, Form, CardBody, Modal, Button } from 'react-bootstrap';
import Footer from '../../components/Footer';
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";
import profileimg from "/src/assets/images/avtar/profile-img.png"
import emailicon from "/src/assets/images/icon/email-subscription.png"
import trophyicon from "/src/assets/images/icon/tropphy.png"
import clockicon from "/src/assets/images/workout/clock.svg"
import fireicon from "/src/assets/images/workout/fire.svg"
import powericon from "/src/assets/images/workout/power.svg"
import { IconChevronRight, IconCircleArrowUp, IconX } from "@tabler/icons-react";

export default function Profile() {
    const [startDate, setStartDate] = useState(new Date());

    const [settings, setSettings] = useState({
        notification: true,
        pinLock: true,
        appleHealth: true,
        darkMode: false,
    });

    const handleToggle = (settingName) => {
        setSettings(prev => ({
            ...prev,
            [settingName]: !prev[settingName],
        }));
    };

    const [showModal, setShowModal] = useState(false);
    const [schedule, setSchedule] = useState('');

    const handleSchedule = () => {
        console.log('Workout scheduled for:', schedule);
        setShowModal(false);
    };
    return (
        <>

            {/* Theme Body Start */}
            <main className="themebody-wrap">
                <div className="theme-body">
                    <Container fluid>
                        <Row>
                            <Col xxl={4} md={6}>
                                <Card>
                                    <CardBody>
                                        <div className="d-flex align-items-center gap-3">
                                            <div className="img-wrap border rounded-2 px-3 pt-2">
                                                <img src={profileimg} alt="" className="img-fluid" />
                                            </div>
                                            <div>
                                                <h4 className="fw-bold mb-2">Leslie Alexander</h4>
                                                <Link to="#" onClick={(e) => e.preventDefault()} className="btn btn-primary">Go Premium</Link>
                                            </div>
                                        </div>
                                        <ul className="equipment-list gap-3 my-4">
                                            <li>
                                                <Link to="#" onClick={(e) => e.preventDefault()}>
                                                    <img src={clockicon} alt="" className="img-fluid" />
                                                    55 kg
                                                </Link>
                                            </li>
                                            <li>
                                                <Link to="#" onClick={(e) => e.preventDefault()}>
                                                    <img src={fireicon} alt="" className="img-fluid" />
                                                    167 cm
                                                </Link>
                                            </li>
                                            <li>
                                                <Link to="#" onClick={(e) => e.preventDefault()}>
                                                    <img src={powericon} alt="" className="img-fluid" />
                                                    26 years
                                                </Link>
                                            </li>
                                        </ul>
                                        <table className="table mt-3 mb-4">
                                            <tbody>
                                                <tr>
                                                    <td className="fs-6 fw-semibold py-3 align-middle">Name</td>
                                                    <td className="text-end align-middle">
                                                        <span>
                                                            Leslie Alexander                                                            
                                                            <IconChevronRight className="ms-2 fs-5 align-middle"/>
                                                        </span>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td className="fs-6 fw-semibold py-3 align-middle">Gender</td>
                                                    <td className="text-end align-middle">
                                                        <span>
                                                            Male 
                                                            <IconChevronRight className="ms-2 fs-5 align-middle"/>
                                                        </span>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td className="fs-6 fw-semibold py-3 align-middle">Date of birth</td>
                                                    <td className="text-end align-middle">
                                                        <span>22 may 1995 <IconChevronRight className="ms-2 fs-5 align-middle"/>
                                                        </span>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td className="fs-6 fw-semibold py-3 align-middle">Height</td>
                                                    <td className="text-end align-middle">
                                                        <span>172 cm <IconChevronRight className="ms-2 fs-5 align-middle"/>
                                                        </span>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td className="fs-6 fw-semibold py-3 align-middle">Weight</td>
                                                    <td className="text-end align-middle">
                                                        <span>62 kg <IconChevronRight className="ms-2 fs-5 align-middle"/>
                                                        </span>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td className="fs-6 fw-semibold py-3 align-middle">Max heart rate</td>
                                                    <td className="text-end align-middle">
                                                        <span>195 bpm <IconChevronRight className="ms-2 fs-5 align-middle"/>
                                                        </span>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </CardBody>
                                </Card>
                                <Card className="bg-light-warning">
                                    <CardBody>
                                        <img src={emailicon} alt="" className="img-fluid" />
                                        <h3 className="fw-bold mt-4">Subscribe to Our Newsletter</h3>
                                        <p className="mt-3">Subscribe to our newsletter for the latest updates, exclusive content, and special offers delivered directly to your inbox</p>
                                        <input type="text" placeholder="Enter Your Email" className="form-control mt-5 border-0 p-3" />
                                        <Link to="#" onClick={(e) => e.preventDefault()} className="btn btn-primary mt-4 w-100 py-3">Subscribe</Link>
                                    </CardBody>
                                </Card>
                            </Col>
                            <Col xxl={4} md={6}>
                                <Card>
                                    <CardBody>
                                        <h4 className="fw-bold mb-3">Account</h4>
                                        <table className="table">
                                            <tbody>
                                                <tr onClick={() => setShowModal(true)}>
                                                    <td className="fs-6 fw-semibold py-3 align-middle">Account</td>
                                                    <td className="text-end align-middle">
                                                        <span className="fs-5">
                                                            <IconChevronRight className="ms-2 fs-5 align-middle"/>
                                                        </span>
                                                    </td>
                                                </tr>
                                                <tr onClick={() => setShowModal(true)}>
                                                    <td className="fs-6 fw-semibold py-3 align-middle">My Workouts</td>
                                                    <td className="text-end align-middle">
                                                        <span className="fs-5">
                                                            <IconChevronRight className="ms-2 fs-5 align-middle"/>
                                                        </span>
                                                    </td>
                                                </tr>
                                                <tr onClick={() => setShowModal(true)}>
                                                    <td className="fs-6 fw-semibold py-3 align-middle">Workout reminders</td>
                                                    <td className="text-end align-middle">
                                                        <span className="fs-5">
                                                            <IconChevronRight className="ms-2 fs-5 align-middle"/>
                                                        </span>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                        <Link to="#" onClick={(e) => e.preventDefault()} className="btn btn-primary mt-3">Log out</Link>
                                    </CardBody>
                                </Card>
                                <Card>
                                    <CardBody>
                                        <h4 className="fw-bold mb-3">Weight tracking</h4>
                                        <ul className="warmup-list mb-3">
                                            <li>
                                                <div className="d-flex align-items-center justify-content-between">
                                                    <div>
                                                        <span className="font-light fs-6">Toady</span>
                                                        <h6 className="fw-bold">52.7 kg</h6>
                                                    </div>
                                                    <div className="text-end">
                                                        <span className="font-light fs-6">7:02</span>
                                                        <h6 className="fw-bold">
                                                            +0.1 kg                                                            
                                                            <IconCircleArrowUp className="align-middle text-danger ms-1 fs-5"/>
                                                        </h6>
                                                    </div>
                                                </div>
                                            </li>
                                            <li>
                                                <div className="d-flex align-items-center justify-content-between">
                                                    <div>
                                                        <span className="font-light fs-6">Nov 13,2028</span>
                                                        <h6 className="fw-bold">52.6 kg</h6>
                                                    </div>
                                                    <div className="text-end">
                                                        <span className="font-light fs-6">8:46</span>
                                                        <h6 className="fw-bold">
                                                            -0.3 kg
                                                            <IconCircleArrowUp className="align-middle text-danger ms-1 fs-5"/>
                                                        </h6>
                                                    </div>
                                                </div>
                                            </li>
                                            <li>
                                                <div className="d-flex align-items-center justify-content-between">
                                                    <div>
                                                        <span className="font-light fs-6">Nov 14,2028</span>
                                                        <h6 className="fw-bold">52.9 kg</h6>
                                                    </div>
                                                    <div className="text-end">
                                                        <span className="font-light fs-6">9:22</span>
                                                        <h6 className="fw-bold">
                                                            +0.2 kg                                                           
                                                            <IconCircleArrowUp className="align-middle text-danger ms-1 fs-5"/>
                                                        </h6>
                                                    </div>
                                                </div>
                                            </li>
                                            <li>
                                                <div className="d-flex align-items-center justify-content-between">
                                                    <div>
                                                        <span className="font-light fs-6">Nov 15,2028</span>
                                                        <h6 className="fw-bold">52.7 kg</h6>
                                                    </div>
                                                    <div className="text-end">
                                                        <span className="font-light fs-6">6:50</span>
                                                        <h6 className="fw-bold">
                                                            -0.2 kg
                                                            <IconCircleArrowUp className="align-middle text-danger ms-1 fs-5"/>
                                                        </h6>
                                                    </div>
                                                </div>
                                            </li>
                                            <li>
                                                <div className="d-flex align-items-center justify-content-between">
                                                    <div>
                                                        <span className="font-light fs-6">Nov 16,2028</span>
                                                        <h6 className="fw-bold">52.9 kg</h6>
                                                    </div>
                                                    <div className="text-end">
                                                        <span className="font-light fs-6">7:40</span>
                                                        <h6 className="fw-bold">
                                                            -0.1 kg
                                                            <IconCircleArrowUp className="align-middle text-danger ms-1 fs-5"/>
                                                        </h6>
                                                    </div>
                                                </div>
                                            </li>
                                            <li>
                                                <div className="d-flex align-items-center justify-content-between">
                                                    <div>
                                                        <span className="font-light fs-6">Nov 17,2028</span>
                                                        <h6 className="fw-bold">52.9 kg</h6>
                                                    </div>
                                                    <div className="text-end">
                                                        <span className="font-light fs-6">6:25</span>
                                                        <h6 className="fw-bold">
                                                            -0.1 kg                                                           
                                                            <IconCircleArrowUp className="align-middle text-danger ms-1 fs-5"/>
                                                        </h6>
                                                    </div>
                                                </div>
                                            </li>
                                            <li>
                                                <div className="d-flex align-items-center justify-content-between">
                                                    <div>
                                                        <span className="font-light fs-6">Nov 15,2028</span>
                                                        <h6 className="fw-bold">52.7 kg</h6>
                                                    </div>
                                                    <div className="text-end">
                                                        <span className="font-light fs-6">6:50</span>
                                                        <h6 className="fw-bold">
                                                            -0.2 kg
                                                            <IconCircleArrowUp className="align-middle text-danger ms-1 fs-5"/>
                                                        </h6>
                                                    </div>
                                                </div>
                                            </li>
                                        </ul>
                                    </CardBody>
                                </Card>
                            </Col>
                            <Col xxl={4} lg={12}>
                                <Row>
                                    <Col xxl={12} md={4} className="order-xxl-3 order-3">
                                        <Card className="bg-light-warning">
                                            <CardBody>
                                                <div className="d-flex align-items-center justify-content-between gap-2">
                                                    <div>
                                                        <h3 className="mb-2 fw-bold">Fitness Star</h3>
                                                        <p>Got an award for your calorie-burning journey</p>
                                                    </div>
                                                    <img src={trophyicon} alt="" className="img-fluid" />
                                                </div>
                                            </CardBody>
                                        </Card>
                                    </Col>
                                    <Col xxl={12} md={6}>
                                        <Card>
                                            <CardBody>
                                                <h4 className="fw-bold mb-2">Settings</h4>
                                                <table className="table mt-3 mb-0">
                                                    <tbody>
                                                        <tr>
                                                            <td className="fs-6 fw-semibold py-4 align-middle">Preferences</td>
                                                            <td className="text-end align-middle">
                                                                <span className="fs-5">
                                                                    <IconChevronRight className="ms-2 fs-5 align-middle"/>
                                                                </span>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td className="fs-6 fw-semibold py-4 align-middle">Plan Settings</td>
                                                            <td className="text-end align-middle">
                                                                <span className="fs-5">
                                                                    <IconChevronRight className="ms-2 fs-5 align-middle"/>
                                                                </span>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td className="fs-6 fw-semibold py-4 align-middle">Notification</td>
                                                            <td className="text-end align-middle">
                                                                <label className="switch round switch-primary">
                                                                    <input
                                                                        type="checkbox"
                                                                        checked={settings.notification}
                                                                        onChange={() => handleToggle('notification')}
                                                                    />
                                                                    <span className="switch-btn"></span>
                                                                </label>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td className="fs-6 fw-semibold py-4 align-middle">Pin Lock</td>
                                                            <td className="text-end align-middle">
                                                                <label className="switch round switch-primary">
                                                                    <input
                                                                        type="checkbox"
                                                                        checked={settings.pinLock}
                                                                        onChange={() => handleToggle('pinLock')}
                                                                    />
                                                                    <span className="switch-btn"></span>
                                                                </label>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td className="fs-6 fw-semibold py-4 align-middle">Apple Health</td>
                                                            <td className="text-end align-middle">
                                                                <label className="switch round switch-primary">
                                                                    <input
                                                                        type="checkbox"
                                                                        checked={settings.appleHealth}
                                                                        onChange={() => handleToggle('appleHealth')}
                                                                    />
                                                                    <span className="switch-btn"></span>
                                                                </label>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td className="fs-6 fw-semibold py-4 align-middle">Dark Mode</td>
                                                            <td className="text-end align-middle">
                                                                <label className="switch round switch-primary">
                                                                    <input
                                                                        type="checkbox"
                                                                        checked={settings.darkMode}
                                                                        onChange={() => handleToggle('darkMode')}
                                                                    />
                                                                    <span className="switch-btn"></span>
                                                                </label>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td className="fs-6 fw-semibold pt-4 border-bottom-0 align-middle">Contacts Support</td>
                                                            <td className="text-end border-bottom-0 pt-4 align-middle">
                                                                <span className="fs-5">
                                                                    <IconChevronRight className="ms-2 fs-5 align-middle"/>
                                                                </span>
                                                            </td>
                                                        </tr>
                                                    </tbody>
                                                </table>
                                            </CardBody>
                                        </Card>
                                    </Col>
                                    <Col xxl={12} md={6}>
                                        <Card>
                                            <CardBody>
                                                <h4 className="fw-bold">Music Provider</h4>
                                                <p className="mb-3">Listen to your favorite music while exercising. The best music streaming services include access to millions of songs.</p>
                                                <table className="table">
                                                    <tbody>
                                                        <tr>
                                                            <td className="fs-6 fw-semibold py-3 align-middle">Spotily</td>
                                                            <td className="text-end align-middle">
                                                                <span className="fs-5">
                                                                    <IconChevronRight className="ms-2 fs-5 align-middle"/>
                                                                </span>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td className="fs-6 fw-semibold py-3 align-middle">Apple Music</td>
                                                            <td className="text-end align-middle">
                                                                <span className="fs-5">
                                                                    <IconChevronRight className="ms-2 fs-5 align-middle"/>
                                                                </span>
                                                            </td>
                                                        </tr>
                                                    </tbody>
                                                </table>
                                            </CardBody>
                                        </Card>
                                    </Col>
                                </Row>
                            </Col>
                        </Row>
                    </Container>
                </div>

                <Modal show={showModal} onHide={() => setShowModal(false)} centered>
                    <Modal.Body>
                        <span
                            className="close-modal"
                            onClick={() => setShowModal(false)}
                            style={{ float: 'right', cursor: 'pointer' }}
                        >                           
                            <IconX/>
                        </span>
                        <h3 className="text-center fw-bold mt-2">Workout reminders</h3>
                        <p className="mb-3 text-center">Select the days you want to exercise</p>
                        <DatePicker className="form-control w-100" selected={startDate} onChange={(date) => setStartDate(date)} />
                        <Button className="btn-primary mt-4 w-100" onClick={handleSchedule}>
                            Schedule
                        </Button>
                    </Modal.Body>
                </Modal>

            </main>
            {/* Theme Body End */}

            {/* Footer Start */}
            <Footer />
            {/* Footer End */}

        </>
    );
}
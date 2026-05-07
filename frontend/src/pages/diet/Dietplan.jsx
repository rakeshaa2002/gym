import React, { useState, useEffect } from 'react';
import { Link } from "react-router-dom";
import { Row, Col, Card, Button, Container, Form, CardBody, InputGroup, Badge, ListGroup, Modal } from 'react-bootstrap';
import Footer from '../../components/Footer.jsx';
import Dietdata from '../api/Dietdata.json';
import {getImageUrl} from "../../utils/imageresolver.js"
import { IconBookmarks, IconChartBar, IconClock, IconDots, IconDroplet, IconDropletHalf2, IconFish, IconPhotoUp, IconSearch, IconStarFilled } from '@tabler/icons-react';
export default function Dietplan() {

    const [showModal, setShowModal] = useState(false);
    const [formData, setFormData] = useState({
        name: '',
        mealType: '',
        energyType: '',
        description: '',
        file: null,
    });

    const handleChange = (e) => {
        const { name, value, files } = e.target;
        setFormData((prev) => ({
            ...prev,
            [name]: files ? files[0] : value,
        }));
    };

    const handleSave = () => {
        console.log(formData);
        setShowModal(false);
        // Add further save logic here (e.g., API call)
    };

    return (
        <>

            {/* Theme Body Start */}
            <main className="themebody-wrap">

                <div className="theme-body">
                    <Container fluid>
                        <Row>
                            <Col xxl={8}>
                                <Card>
                                    <CardBody>
                                        <Row className="align-items-center gy-4">
                                            {/* Search and Filter */}
                                            <Col md={6}>
                                                <InputGroup>
                                                    <InputGroup.Text className="pe-0">                                                        
                                                        <IconSearch/>
                                                    </InputGroup.Text>
                                                    <Form.Control type="text" placeholder="Search for menu" />
                                                </InputGroup>
                                            </Col>
                                            <Col md={6}>
                                                <div className="d-flex justify-content-end align-items-center gap-3">
                                                    <Form.Select className="w-50">
                                                        <option>All</option>
                                                        <option>Breakfast</option>
                                                        <option>Lunch</option>
                                                        <option>Snack</option>
                                                        <option>Dinner</option>
                                                    </Form.Select>
                                                    <Button variant="primary" onClick={() => setShowModal(true)}>
                                                        Add Menu
                                                    </Button>
                                                </div>
                                            </Col>

                                            {/* Diet Cards from JSON */}                                           
                                            {Dietdata.diets.map((diet, idx) => {
                                                    const imageUrl = getImageUrl(diet.image);
                                                return (
                                                <Col md={12} sm={6} key={idx}>
                                                    <div className="dite-grid">
                                                        <div className="img-wrap">
                                                            <Link to="/diet-detail">
                                                                <img src={imageUrl} alt="" className="img-fluid" />
                                                            </Link>
                                                        </div>
                                                        <div className="dite-detail">
                                                            <div className="d-flex flex-wrap gap-2 align-items-center justify-content-between mb-4">
                                                                <span className={`badge ${diet.badgeClass}`}>{diet.type}</span>
                                                                <div className="d-flex flex-nowrap gap-1">
                                                                    <span className="badge badge-light">
                                                                        <IconChartBar className='me-2'/>
                                                                        {diet.difficulty}
                                                                    </span>
                                                                    <span className="badge badge-light">                                                                       
                                                                        <IconClock/>
                                                                        {diet.time}
                                                                    </span>
                                                                </div>
                                                            </div>
                                                            <Link to="/diet-detail">
                                                                <h3 className="mb-2 fw-bold">{diet.title}</h3>
                                                            </Link>
                                                            <p className="mb-3">{diet.description}</p>
                                                            <p className="mb-3">Health Score: {diet.healthScore}/100</p>
                                                            <div className="d-flex align-items-center justify-content-between">
                                                                <ul className="helthscor-list">
                                                                    {[...Array(10)].map((_, i) => (
                                                                        <li key={i} className={i < diet.healthScore / 10 ? 'active' : ''}></li>
                                                                    ))}
                                                                </ul>
                                                            </div>
                                                        </div>
                                                        <div className="food-vitamin">
                                                            <ul className="vitamin-list">
                                                                <li>                                                                   
                                                                    <IconDropletHalf2 className='me-2'/>
                                                                    {diet.calories}</li>
                                                                <li>                                                                   
                                                                    <IconBookmarks className='me-2'/>
                                                                    {diet.carbs}</li>
                                                                <li>                                                                   
                                                                    <IconFish className='me-2'/>                                                                   
                                                                    {diet.protein}</li>
                                                                <li>                                                                   
                                                                    <IconDroplet className='me-2'/>
                                                                    {diet.fats}</li>
                                                            </ul>
                                                        </div>
                                                    </div>
                                                </Col>
                                                );
                                            })}
                                        </Row>
                                    </CardBody>
                                </Card>
                            </Col>
                            <Col xxl={4}>
                                <Row>
                                    <Col xxl={12} md={6}>
                                        <Card>
                                            <Card.Body>
                                                <div className="d-flex align-items-center justify-content-between mb-2">
                                                    <h4 className="fw-bold">Popular Menu</h4>
                                                    <Link to="#" onClick={(e) => e.preventDefault()} className="fs-4">                                                       
                                                        <IconDots/>
                                                    </Link>
                                                </div>
                                                <Row className="gy-4">                                                   
                                                    {Dietdata.popularMenus.map((item, idx) => {
                                                        const imageUrl = getImageUrl(item.image);
                                                        return (
                                                            <Col md={12} key={idx}>
                                                                <div className="dite-grid dite-smgrid">
                                                                    <div className="img-wrap">
                                                                        <Link to="/diet-detail">
                                                                            <img src={imageUrl} alt={item.title} className="img-fluid" />
                                                                        </Link>
                                                                    </div>
                                                                    <div className="diet-detail">
                                                                        <Link to="/diet-detail">
                                                                            <h4 className="fw-bold mb-4">{item.title}</h4>
                                                                        </Link>
                                                                        <span className="badge badge-border-light">                                                                           
                                                                            <IconStarFilled className='text-warning'/>
                                                                            {item.rating}
                                                                        </span>
                                                                        <span className={`badge ms-2 ${item.badgeClass}`}>{item.type}</span>
                                                                    </div>
                                                                </div>
                                                            </Col>
                                                        );
                                                    })}                                                   
                                                </Row>
                                            </Card.Body>
                                        </Card>
                                    </Col>
                                    <Col xxl={12} md={6}>
                                        <Card>
                                            <Card.Body>
                                                <div className="d-flex align-items-center justify-content-between mb-2">
                                                    <h4 className="fw-bold">Recommended Menu</h4>
                                                    <Link to="#" onClick={(e) => e.preventDefault()} className="fs-4">                                                       
                                                        <IconDots/>
                                                    </Link>
                                                </div>
                                                <Row className="gy-4">
                                                    {Dietdata.recommendedmenus.map((item, idx) => {
                                                        const imageUrl = getImageUrl(item.image);
                                                        return(
                                                            <Col md={12} key={idx}>
                                                                <div className="dite-grid dite-smgrid d-grid">
                                                                    <div className="d-md-flex d-grid align-items-center gap-3">
                                                                        <div className="img-wrap">
                                                                            <Link to="/diet-detail">
                                                                                <img src={imageUrl} alt={item.title} className='img-fluiod'/>
                                                                            </Link>
                                                                        </div>
                                                                        <div className="diet-detail">
                                                                            <Link to="/diet-detail">
                                                                                <h4 className="fw-bold mb-4">{item.title}</h4>
                                                                            </Link>
                                                                            <Badge bg="warning" className="me-2">
                                                                                {item.type}
                                                                            </Badge>
                                                                            <Badge bg="light" text="dark">                                                                               
                                                                                <IconChartBar className='me-2' />
                                                                                {item.difficulty}
                                                                            </Badge>
                                                                        </div>
                                                                    </div>
                                                                    <div className="border-top mt-3 pt-2">
                                                                        <ListGroup horizontal className="justify-content-around font-light">
                                                                            <ListGroup.Item className="border-0 px-2">{item.calories}</ListGroup.Item>
                                                                            <ListGroup.Item className="border-0 px-2">{item.carbs}</ListGroup.Item>
                                                                            <ListGroup.Item className="border-0 px-2">{item.protein}</ListGroup.Item>
                                                                            <ListGroup.Item className="border-0 px-2">{item.fats}</ListGroup.Item>
                                                                        </ListGroup>
                                                                    </div>
                                                                </div>
                                                            </Col>
                                                        );
                                                    })}
                                                </Row>
                                            </Card.Body>
                                        </Card>
                                    </Col>
                                </Row>
                            </Col>
                        </Row>
                    </Container>
                </div>


                <Modal className='diatmenu_modal' show={showModal} onHide={() => setShowModal(false)} size="lg" centered>
                    <Modal.Body>
                        <Form>
                            <Row className="gy-3">
                                <Col md={3}>
                                    <Form.Label htmlFor="dietImg" className="file-upload d-flex justify-content-center align-items-center">                                       
                                        <IconPhotoUp/>
                                    </Form.Label>
                                    <Form.Control type="file" id="dietImg" name="file" onChange={handleChange} hidden />
                                </Col>

                                <Col md={9}>
                                    <Row className="gy-3">
                                        <Col md={12}>
                                            <Form.Label>Name</Form.Label>
                                            <Form.Control
                                                type="text"
                                                name="name"
                                                placeholder="Healthy Eating"
                                                value={formData.name}
                                                onChange={handleChange}
                                            />
                                        </Col>
                                        <Col md={6}>
                                            <Form.Label>Meal Type</Form.Label>
                                            <Form.Select name="mealType" value={formData.mealType} onChange={handleChange}>
                                                <option value="">Select</option>
                                                <option>Breakfast</option>
                                                <option>Lunch</option>
                                                <option>Snack</option>
                                                <option>Dinner</option>
                                            </Form.Select>
                                        </Col>
                                        <Col md={6}>
                                            <Form.Label>Energy Type</Form.Label>
                                            <Form.Select name="energyType" value={formData.energyType} onChange={handleChange}>
                                                <option value="">Select</option>
                                                <option>Easy</option>
                                                <option>Medium</option>
                                                <option>Hard</option>
                                            </Form.Select>
                                        </Col>
                                    </Row>
                                </Col>

                                <Col md={12}>
                                    <Form.Label>Describe</Form.Label>
                                    <Form.Control
                                        as="textarea"
                                        rows={3}
                                        name="description"
                                        placeholder="Write your message here..."
                                        value={formData.description}
                                        onChange={handleChange}
                                    />
                                </Col>
                                <Col md={12}>
                                    <div className="d-flex justify-content-end gap-3">
                                        <Button variant="primary" onClick={handleSave}>
                                            Save
                                        </Button>
                                        <Button variant="outline-secondary" onClick={() => setShowModal(false)}>
                                            Cancel
                                        </Button>
                                    </div>
                                </Col>
                            </Row>
                        </Form>
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
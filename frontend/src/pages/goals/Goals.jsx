import React from 'react';
import { Table, Container, Row, Col, Card, CardBody } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import Footer from '../../components/Footer.jsx';
import Goaldata from '../api/Goalsdata.json';
import {getImageUrl} from "../../utils/imageresolver.js"
import { IconPlus, IconSearch } from '@tabler/icons-react';

export default function Goals() {
    return (
        <>
            <main className="themebody-wrap">
                <div className="theme-body goals-page">
                    <Container fluid>
                        <Card>
                            <CardBody>
                                <Row className="gy-4">
                                    {/* Search and Filters */}
                                    <Col md={8}>
                                        <div className="d-sm-flex align-items-center justify-content-between gap-3">
                                            <div className="input-group">
                                                <span className="input-group-text pe-0">                                                   
                                                    <IconSearch/>
                                                </span>
                                                <input type="text" placeholder="Search for menu" className="form-control" />
                                            </div>
                                            <div className="d-sm-flex align-items-center gap-3">
                                                <select className="form-select select-status">
                                                    <option value="">Status</option>
                                                </select>
                                                <select className="form-select select-week">
                                                    <option value="">This Week</option>
                                                </select>
                                            </div>
                                        </div>
                                    </Col>
                                    {/* Add Exercise */}
                                    <Col md={4} className="text-end">
                                        <Link to="#" onClick={(e) => e.preventDefault()} className="btn btn-primary">                                           
                                            <IconPlus/>
                                            Add Exercise
                                        </Link>
                                    </Col>

                                    {/* Table */}
                                    <Col className="mt-4">
                                        <Table className="goals-table" responsive>
                                            <thead>
                                                <tr>
                                                    <th>Exercise Name</th>
                                                    <th>Sets</th>
                                                    <th>Reps</th>
                                                    <th>Rest</th>
                                                    <th>Weight</th>
                                                    <th>Calories</th>
                                                    <th className="text-end">Status</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {Goaldata.map((ex, idx) => {
                                                    const imageUrl = getImageUrl(ex.img);
                                                    return(
                                                        <tr key={idx}>
                                                            <td>
                                                                <div className="cooking-grid">
                                                                    <div className="icon-wrap">
                                                                        <img src={imageUrl} alt={ex.name} className="img-fluid" />
                                                                    </div>
                                                                    {ex.name}
                                                                </div>
                                                            </td>
                                                            <td>{ex.sets}</td>
                                                            <td>{ex.reps} <span className="font-light">repetitions</span></td>
                                                            <td>{ex.rest} <span className="font-light">seconds</span></td>
                                                            <td>{ex.weight} <span className="font-light">kg</span></td>
                                                            <td>{ex.calories} <span className="font-light">cal</span></td>
                                                            <td className="text-end">
                                                            <span className={`badge ${
                                                                    ex.status === 'Completed' ? 'badge-success' :
                                                                    ex.status === 'In Progress' ? 'badge-danger' :
                                                                    ex.status === 'Skipped' ? 'badge-danger' :
                                                                    ex.status === 'Not Started' ? 'badge-warning' :
                                                                    'badge-secondary' // fallback if status is unknown
                                                                    }`}>
                                                                    {ex.status}
                                                                    </span>
                                                            </td>
                                                        </tr>
                                                    );
                                                })}
                                            </tbody>
                                        </Table>
                                    </Col>
                                </Row>
                            </CardBody>
                        </Card>
                    </Container>
                </div>
            </main>
            <Footer />
        </>
    );
}

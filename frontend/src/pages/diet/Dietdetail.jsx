import React, { useState, useEffect } from 'react';
import { Link } from "react-router-dom";
import { Row, Col, Card, Table, Container, Form, CardBody } from 'react-bootstrap';
import Slider from "react-slick";
import Footer from '../../components/Footer';

import diet1 from "/src/assets/images/diet-plan/diet1.png"
import diet2 from "/src/assets/images/diet-plan/diet2.png"
import diet3 from "/src/assets/images/diet-plan/diet3.png"
import testimonialavtar from "/src/assets/images/avtar/samantha-lee.png"
import { IconChartBar, IconGrill, IconHeartBroken, IconListNumbers, IconMinus, IconPlus, IconPointFilled, IconSlice, IconStarFilled, IconToolsKitchen2Off } from '@tabler/icons-react';

export default function Dietdetail() {
    const [value, setValue] = useState("1");
    var wokoutslider = {
        infinite: true,
        slidesToShow: 2,
        slidesToScroll: 1,
        speed: 1000,
        autoplay: true,
        autoplaySpeed: 1500,
        responsive: [
            {
                breakpoint: 1441,
                settings: {
                    slidesToShow: 3,
                }
            },
            {
                breakpoint: 768,
                settings: {
                    slidesToShow: 2,
                }
            },
            {
                breakpoint: 481,
                settings: {
                    slidesToShow: 1,
                }
            }
        ]
    };
    var testimonialslider = {
        infinite: true,
        slidesToShow: 4,
        slidesToScroll: 1,
        infinite: true,
        speed: 1000,
        autoplay: true,
        autoplaySpeed: 1600,
        responsive: [
            {
                breakpoint: 1441,
                settings: {
                    slidesToShow: 3,
                }
            },
            {
                breakpoint: 992,
                settings: {
                    slidesToShow: 2,
                }
            },
            {
                breakpoint: 768,
                settings: {
                    slidesToShow: 1,
                }
            }
        ]
    };
    return (
        <>

            {/* Theme Body Start */}
            <main className="themebody-wrap">

                <div className="theme-body">
                    <Container fluid>
                        <Row>
                            <div className="col-xxl-8">
                                <div className="card">
                                    <div className="card-body">
                                        <div className="row gy-4">
                                            <div className="col-md-12">
                                                <Slider {...wokoutslider} className="popularworkout-slider arrow-style1">
                                                    <div>
                                                        <div className="workout-grid">
                                                            <img src={diet1} alt="" className="img-fluid w-100" />
                                                        </div>
                                                    </div>
                                                    <div>
                                                        <div className="workout-grid">
                                                            <img src={diet2} alt="" className="img-fluid w-100" />
                                                        </div>
                                                    </div>
                                                    <div>
                                                        <div className="workout-grid">
                                                            <img src={diet3} alt="" className="img-fluid w-100" />
                                                        </div>
                                                    </div>
                                                </Slider>
                                            </div>
                                            <div className="col-md-12">
                                                <div className="d-flex align-items-center justify-content-between mb-2">
                                                    <span className="badge badge-primary">Breakfast</span>
                                                    <span>                                                       
                                                        <IconStarFilled className='text-warning'/>
                                                        4.8/5 (+220 reviews)
                                                    </span>
                                                </div>
                                                <h3 className="mb-2 fw-bold">Scrambled Eggs with Turkey Bacon and Sauteed Spinach</h3>
                                                <p>This nutritious breakfast combines high-quality protein and healthy fats to fuel your day. The scrambled eggs provide essential amino acids, while the turkey bacon adds a lean source of protein. Sautéed spinach contributes vitamins and minerals, making this dish both satisfying and health-conscious.</p>
                                                <div className="row gy-4 mt-0">
                                                    <div className="col-md-4 col-6">
                                                        <div className="cooking-grid">
                                                            <div className="icon-wrap">                                                               
                                                                <IconToolsKitchen2Off/>
                                                            </div>
                                                            <div>
                                                                <p>Eat Time</p>
                                                                <strong>8:00 AM</strong>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div className="col-md-4 col-6">
                                                        <div className="cooking-grid">
                                                            <div className="icon-wrap">
                                                                <IconSlice/>
                                                            </div>
                                                            <div>
                                                                <p>Prep Time</p>
                                                                <strong>5 minutes</strong>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div className="col-md-4 col-6">
                                                        <div className="cooking-grid">
                                                            <div className="icon-wrap">                                                               
                                                                <IconGrill/>
                                                            </div>
                                                            <div>
                                                                <p>Cook Time</p>
                                                                <strong>10 minutes</strong>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div className="col-md-4 col-6">
                                                        <div className="cooking-grid">
                                                            <div className="icon-wrap">                                                               
                                                                <IconChartBar/>
                                                            </div>
                                                            <div>
                                                                <p>Difficulty</p>
                                                                <strong>Medium</strong>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div className="col-md-4 col-6">
                                                        <div className="cooking-grid">
                                                            <div className="icon-wrap">                                                               
                                                                <IconListNumbers />
                                                            </div>
                                                            <div>
                                                                <p>Total Steps</p>
                                                                <strong>4 Steps</strong>
                                                            </div>
                                                        </div>
                                                    </div>
                                                    <div className="col-md-4 col-6">
                                                        <div className="cooking-grid">
                                                            <div className="icon-wrap">                                                               
                                                                <IconHeartBroken />
                                                            </div>
                                                            <div>
                                                                <p>Health Score</p>
                                                                <strong>85/100</strong>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div className="card">
                                    <div className="card-body">
                                        <div className="row">
                                            <div className="col-md-6">
                                                <h4 className="fw-bold mb-4">Directions</h4>
                                                <ul className="direction-list">
                                                    <li>
                                                        <div className="number-wrap">1</div>
                                                        <h5 className="fw-semibold">Prep the Ingredients</h5>
                                                        <p>Crack the eggs into a mixing bowl, add a pinch of salt and pepper, and whisk until fully blended.</p>
                                                    </li>
                                                    <li>
                                                        <div className="number-wrap">2</div>
                                                        <h5 className="fw-semibold">Cook the Turkey Bacon</h5>
                                                        <p>Heat the skillet over medium heat and cook the turkey bacon until crispy, about 3-4 minutes on each side. Remove and set aside.</p>
                                                    </li>
                                                    <li>
                                                        <div className="number-wrap">3</div>
                                                        <h5 className="fw-semibold">Saute the Spinach</h5>
                                                        <p>In the same skillet, add olive oil and spinach. Saute until the spinach is wilted, about 2-3 minutes. Remove and set aside.</p>
                                                    </li>
                                                    <li>
                                                        <div className="number-wrap">4</div>
                                                        <h5 className="fw-semibold">Scramble the Eggs</h5>
                                                        <p>Pour the egg mixture into the skillet and cook, stirring gently with a spatula, until the eggs are fully cooked but still soft, about 2-3 minutes.</p>
                                                    </li>
                                                    <li>
                                                        <div className="number-wrap">5</div>
                                                        <h5 className="fw-semibold">Assemble and Serve</h5>
                                                        <p>Plate the scrambled eggs with turkey bacon and sauteed spinach. Serve immediately.</p>
                                                    </li>
                                                </ul>
                                            </div>
                                            <div className="col-md-6">
                                                <div className="ps-md-3 h-100">
                                                    <h4 className="fw-bold mb-4">Tools and Equipments</h4>
                                                    <ul className="toolequipment-list">
                                                        <li>
                                                            <div className="icon-wrap">                                                               
                                                                <IconPointFilled/>
                                                            </div>
                                                            Non - stick skillet
                                                        </li>
                                                        <li>
                                                            <div className="icon-wrap">
                                                                <IconPointFilled/>
                                                            </div>
                                                            Spatula
                                                        </li>
                                                        <li>
                                                            <div className="icon-wrap">
                                                                <IconPointFilled/>
                                                            </div>
                                                            Mixing bow|
                                                        </li>
                                                        <li>
                                                            <div className="icon-wrap">
                                                                <IconPointFilled/>
                                                            </div>
                                                            Fork
                                                        </li>
                                                        <li>
                                                            <div className="icon-wrap">
                                                                <IconPointFilled/>
                                                            </div>
                                                            Measuring spoons
                                                        </li>
                                                    </ul>
                                                    <h4 className="fw-bold mt-4 mb-4">Notes</h4>
                                                    <ul className="toolequipment-list">
                                                        <li className="align-items-start">
                                                            <div className="icon-wrap">
                                                                <IconPointFilled/>
                                                            </div>
                                                            For a lower-calorie option, substitute olive oil with a cooking spray and reduce the amount of turkey bacon.
                                                        </li>
                                                        <li className="align-items-start">
                                                            <div className="icon-wrap">
                                                                <IconPointFilled/>
                                                            </div>
                                                            Add a sprinkle of cheese or herbs like chives or parsley for extra flavor.
                                                        </li>
                                                    </ul>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div className="col-xxl-4">
                                <div className="row">
                                    <div className="col-xxl-12 col-lg-4 col-md-6">
                                        <div className="card bg-primary">
                                            <div className="card-body">
                                                <div className="row gy-4">
                                                    <div className="col-xxl-3 col-6 text-center">
                                                        <h5 className="mb-3 fw-bold text-white text-nowrap">Calories</h5>
                                                        <div className="py-3 rounded-4 bg-white text-center">
                                                            <h5 className="fw-bold">350</h5>
                                                            <span className="font-light">cal</span>
                                                        </div>
                                                    </div>
                                                    <div className="col-xxl-3 col-6 text-center">
                                                        <h5 className="mb-3 fw-bold text-white text-nowrap">Protein</h5>
                                                        <div className="py-3 rounded-4 bg-white text-center">
                                                            <h5 className="fw-bold">25</h5>
                                                            <span className="font-light">gr</span>
                                                        </div>
                                                    </div>
                                                    <div className="col-xxl-3 col-6 text-center">
                                                        <h5 className="mb-3 fw-bold text-white text-nowrap">Carbs</h5>
                                                        <div className="py-3 rounded-4 bg-white text-center">
                                                            <h5 className="fw-bold">10</h5>
                                                            <span className="font-light">gr</span>
                                                        </div>
                                                    </div>
                                                    <div className="col-xxl-3 col-6 text-center">
                                                        <h5 className="mb-3 fw-bold text-white text-nowrap">Fats</h5>
                                                        <div className="py-3 rounded-4 bg-white text-center">
                                                            <h5 className="fw-bold">20</h5>
                                                            <span className="font-light">gr</span>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-xxl-12 col-lg-4 col-md-6">
                                        <div className="card">
                                            <div className="card-body">
                                                <div className="d-flex align-items-center justify-content-between mb-4">
                                                    <div className="cooking-grid">
                                                        <div className="icon-wrap">                                                           
                                                            <IconToolsKitchen2Off/>
                                                        </div>
                                                        <div>
                                                            <p>Eat Time</p>
                                                            <strong>8:00 AM</strong>
                                                        </div>
                                                    </div>
                                                    <div className="counter-group">
                                                        <span className="icon-wrap">                                                           
                                                            <IconMinus/>
                                                        </span>
                                                        <input value={value} onChange={(e) => setValue(e.target.value)} className='form-control' />
                                                        <span className="icon-wrap">                                                           
                                                            <IconPlus/>
                                                        </span>
                                                    </div>
                                                </div>
                                                <h4 className="fw-bold mb-4">Ingredients</h4>
                                                <ul className="toolequipment-list">
                                                    <li>
                                                        <div className="icon-wrap">
                                                            <IconPointFilled/>
                                                        </div>
                                                        2 large eggs
                                                    </li>
                                                    <li>
                                                        <div className="icon-wrap">
                                                            <IconPointFilled/>
                                                        </div>
                                                        2 slices of turkey bacon
                                                    </li>
                                                    <li>
                                                        <div className="icon-wrap">
                                                            <IconPointFilled/>
                                                        </div>
                                                        Mixing bow|
                                                    </li>
                                                    <li>
                                                        <div className="icon-wrap">
                                                            <IconPointFilled/>
                                                        </div>
                                                        1 cup fresh spinach
                                                    </li>
                                                    <li>
                                                        <div className="icon-wrap">
                                                            <IconPointFilled/>
                                                        </div>
                                                        1 tablespoon olive oil
                                                    </li>
                                                    <li>
                                                        <div className="icon-wrap">
                                                            <IconPointFilled/>
                                                        </div>
                                                        Salt and pepper to taste
                                                    </li>
                                                </ul>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-xxl-12 col-lg-4 col-md-12">
                                        <div className="card">
                                            <div className="card-body">
                                                <h4 className="fw-bold mb-3">Nutrition Facts</h4>
                                                <table className="table nutritionfacts-table">
                                                    <tbody>
                                                        <tr>
                                                            <td>Calories</td>
                                                            <td className="text-end">350</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Total Carbohydrates</td>
                                                            <td className="text-end">10 gr</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Protein</td>
                                                            <td className="text-end">25 gr</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Total Fat</td>
                                                            <td className="text-end">20 gr</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Cholesterol</td>
                                                            <td className="text-end">370 mg</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Sodium</td>
                                                            <td className="text-end">720 mg</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Potassium</td>
                                                            <td className="text-end">500 mg</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Vitamin A</td>
                                                            <td className="text-end">120% DV</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Vitamin C</td>
                                                            <td className="text-end">20% DV</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Calcium</td>
                                                            <td className="text-end">10% DV</td>
                                                        </tr>
                                                        <tr>
                                                            <td>Iron</td>
                                                            <td className="text-end">15% DV</td>
                                                        </tr>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div className="col-12">
                                <Slider {...testimonialslider} className="testimonial-slider arrow-style1">
                                    <div>
                                        <div className="testimonial-grid">
                                            <div className="d-flex align-items-center gap-3 mb-4">
                                                <div className="img-wrap">
                                                    <img src={testimonialavtar} alt="" className="img-fluid" />
                                                </div>
                                                <div>
                                                    <h4 className="mb-2">Samantha Lee</h4>
                                                    <span className="fs-6">                                                       
                                                        <IconStarFilled className='text-warning me-2'/>
                                                        5/5
\                                                    </span>
                                                </div>
                                            </div>
                                            <p>My go-to breakfast is quick, filling, and customizable. I love the fresh taste of spinach, and it’s easy to add other veggies or spices!</p>
                                        </div>
                                    </div>
                                    <div>
                                        <div className="testimonial-grid">
                                            <div className="d-flex align-items-center gap-3 mb-4">
                                                <div className="img-wrap">
                                                    <img src={testimonialavtar} alt="" className="img-fluid" />
                                                </div>
                                                <div>
                                                    <h4 className="mb-2">David Chen</h4>
                                                    <span className="fs-6">
                                                        <IconStarFilled className='text-warning me-2'/>                                                       
                                                        4.7/5
                                                    </span>
                                                </div>
                                            </div>
                                            <p>Delicious and healthy. I sometimes add mushrooms for extra veggies. The turkey bacon is a nice, lean alternative to regular bacon.</p>
                                        </div>
                                    </div>
                                    <div>
                                        <div className="testimonial-grid">
                                            <div className="d-flex align-items-center gap-3 mb-4">
                                                <div className="img-wrap">
                                                    <img src={testimonialavtar} alt="" className="img-fluid" />
                                                </div>
                                                <div>
                                                    <h4 className="mb-2">Jessica Moore</h4>
                                                    <span className="fs-6">
                                                        <IconStarFilled className='text-warning me-2'/>
                                                        4.9/5
                                                    </span>
                                                </div>
                                            </div>
                                            <p>Perfect way to start the day! The combination of eggs, spinach, and turkey bacon is both tasty and nutritious. Highly recommend!</p>
                                        </div>
                                    </div>
                                    <div>
                                        <div className="testimonial-grid">
                                            <div className="d-flex align-items-center gap-3 mb-4">
                                                <div className="img-wrap">
                                                    <img src={testimonialavtar} alt="" className="img-fluid" />
                                                </div>
                                                <div>
                                                    <h4 className="mb-2">David Chen</h4>
                                                    <span className="fs-6">
                                                        <IconStarFilled className='text-warning me-2'/>
                                                        4.7/5
                                                    </span>
                                                </div>
                                            </div>
                                            <p>Delicious and healthy. I sometimes add mushrooms for extra veggies. The turkey bacon is a nice, lean alternative to regular bacon.</p>
                                        </div>
                                    </div>
                                    <div>
                                        <div className="testimonial-grid">
                                            <div className="d-flex align-items-center gap-3 mb-4">
                                                <div className="img-wrap">
                                                    <img src={testimonialavtar} alt="" className="img-fluid" />
                                                </div>
                                                <div>
                                                    <h4 className="mb-2">David Chen</h4>
                                                    <span className="fs-6">
                                                        <IconStarFilled className='text-warning me-2'/>
                                                        4.7/5
                                                    </span>
                                                </div>
                                            </div>
                                            <p>Delicious and healthy. I sometimes add mushrooms for extra veggies. The turkey bacon is a nice, lean alternative to regular bacon.</p>
                                        </div>
                                    </div>
                                </Slider>
                            </div>
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
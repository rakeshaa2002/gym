import React, { useEffect, useState, useRef } from "react";
import { Link, useLocation } from "react-router-dom";
import SimpleBar from "simplebar-react";
import { useSidebarContext } from "../context/useSidebarContext";
import { useAuth } from "../context/AuthContext";
import { canAccess } from "../config/pagePermissions";

import logo from "/src/assets/images/logo/logo.png";
import sidebarvactor from "/src/assets/images/pro-sec.png";
import {
  IconBarbell,
  IconCalendar,
  IconCalendarEvent,
  IconChartBar,
  IconChevronLeft,
  IconChevronRight,
  IconEscalatorDown,
  IconKey,
  IconLayoutDashboard,
  IconTargetArrow,
  IconUserStar,
} from "@tabler/icons-react";

const menuPaths = {
  Dashboards: ["/"],
  "Users & Teams": ["/employees", "/users", "/headoffice", "/branches", "/departments", "/designations", "/teams", "/role-permissions"],
  Workout: ["/workout-type", "/body-part", "/exercise-master", "/workout-plan", "/workout-detail", "/create-workout", "/workout-summary"],
  DietPlan: ["/dietplan", "/diet-detail"],
  Calendar: ["/schedule", "/trainer-duty-schedule", "/user-workout-schedule", "/my-schedule"],
  Goals: ["/goals"],
  Progress: ["/progress"],
  Profile: ["/profile"],
  Authentication: ["/sign-in", "/sign-up", "/forgot-password", "/new-password", "/verify-email", "/verify-pin"],
  Step: ["/onboding-step"],
};

export default function Sidebar() {
  const location = useLocation();
  const [activeIndex, setActiveIndex] = useState(null);
  const { toggleSidebar } = useSidebarContext();
  const { permissionMap } = useAuth();

  const menuRef = useRef(null);
  const [leftPos, setLeftPos] = useState(0);
  const [maxScroll, setMaxScroll] = useState(0);
  const [isAtStart, setIsAtStart] = useState(true);
  const [isAtEnd, setIsAtEnd] = useState(false);
  const scrollStep = 150;
  const buffer = -80;

  const canView = (pageKey) => canAccess(permissionMap, pageKey, "view");
  const visibleSettingsItems = [
    { key: "employees", label: "Employees", path: "/employees" },
    { key: "users", label: "Users", path: "/users" },
    { key: "headoffice", label: "Head Office", path: "/headoffice", section: true },
    { key: "branches", label: "Branches", path: "/branches" },
    { key: "departments", label: "Departments", path: "/departments" },
    { key: "designations", label: "Designations", path: "/designations" },
    { key: "teams", label: "Teams", path: "/teams" },
    { key: "role-permissions", label: "Role Permissions", path: "/role-permissions" },
  ].filter((item) => canView(item.key));
  const visibleWorkoutItems = [
    { key: "workout-type", label: "Workout Type Master", path: "/workout-type" },
    { key: "body-part", label: "Body Part Master", path: "/body-part" },
    { key: "exercise-master", label: "Exercise Master", path: "/exercise-master" },
    { key: "workout-plan", label: "Workout Plan Master", path: "/workout-plan" },
    { key: "workout-detail", label: "Workout Detail", path: "/workout-detail" },
  ].filter((item) => canView(item.key));
  const visibleDietItems = [
    { key: "dietplan", label: "Diet Menu", path: "/dietplan" },
    { key: "diet-detail", label: "Diet Detail", path: "/diet-detail" },
  ].filter((item) => canView(item.key));
  const showSettings = visibleSettingsItems.length > 0;
  const showWorkout = visibleWorkoutItems.length > 0;
  const showDiet = visibleDietItems.length > 0;
  const visibleScheduleItems = [
    { key: "trainer-duty-schedule", label: "Trainer Duty Schedule", path: "/trainer-duty-schedule" },
    { key: "user-workout-schedule", label: "User Workout Schedule", path: "/user-workout-schedule" },
    { key: "my-schedule", label: "My Schedule", path: "/my-schedule" },
  ].filter((item) => canView(item.key));
  const showCalendar = visibleScheduleItems.length > 0;
  const showGoals = canView("goals");
  const showProgress = canView("progress");
  const showProfile = canView("profile");

  useEffect(() => {
    const currentPath = location.pathname;
    const category = Object.keys(menuPaths).find((key) => menuPaths[key].includes(currentPath));
    const index = category ? Object.keys(menuPaths).indexOf(category) : null;
    setActiveIndex(index);
  }, [location.pathname]);

  const handleMenuClick = (index) => {
    setActiveIndex((prevIndex) => (prevIndex === index ? null : index));
  };

  const updateScrollLimits = () => {
    const wrapper = menuRef.current?.parentElement;
    const menu = menuRef.current;
    if (wrapper && menu) {
      const wrapperWidth = wrapper.offsetWidth;
      const menuWidth = menu.scrollWidth;
      const max = menuWidth - wrapperWidth;
      setMaxScroll(max > 0 ? max : 0);
      setIsAtStart(leftPos === 0);
      setIsAtEnd(max > 0 && leftPos <= -max + buffer);
    }
  };

  useEffect(() => {
    updateScrollLimits();
    window.addEventListener("resize", updateScrollLimits);
    return () => window.removeEventListener("resize", updateScrollLimits);
  }, [leftPos]);

  useEffect(() => {
    updateScrollLimits();
  }, [maxScroll]);

  const handleScroll = (direction) => {
    let newLeft = leftPos;

    if (direction === "left") {
      newLeft = Math.min(0, leftPos + scrollStep);
    } else if (direction === "right") {
      const maxAllowedLeft = -maxScroll + buffer;
      newLeft = Math.max(leftPos - scrollStep, maxAllowedLeft);
    }

    setLeftPos(newLeft);

    setTimeout(() => {
      setIsAtStart(newLeft === 0);
      setIsAtEnd(maxScroll > 0 && newLeft <= -maxScroll + buffer);
    }, 0);
  };

  return (
    <aside className="codex-sidebar">
      <div className="codex-brand">
        <Link className="d-flex align-items-center" to="/">
          <img className="img-fluid" src={logo} alt="theme-logo" />
          <span className="fs-3 align-middle ms-2 lh-1">FitNexus</span>
        </Link>
        <div className="sidebar-action" onClick={toggleSidebar}>
          <IconChevronLeft />
        </div>
      </div>

      <span className={`menu-preve ${isAtStart ? "disabled" : ""}`} onClick={() => !isAtStart && handleScroll("left")}>
        <IconChevronLeft />
      </span>

      <SimpleBar className="codex-menuwrapper custom-scroll">
        <ul
          className="codex-menu"
          ref={menuRef}
          style={{
            left: `${leftPos}px`,
          }}
        >
          <li onClick={() => handleMenuClick(0)} className={`menu-item ${activeIndex === 0 ? "active" : ""}`}>
            <Link to="/">
              <div className="icon-item">
                <IconLayoutDashboard />
              </div>
              <span>Overview</span>
            </Link>
          </li>

          {showSettings && (
            <li onClick={() => handleMenuClick(1)} className={`menu-item ${activeIndex === 1 ? "active" : ""}`}>
              <Link to="#">
                <div className="icon-item">
                  <IconUserStar />
                </div>
                <span>Settings</span>
                <i className="fa fa-angle-right menu-dropwdown"></i>
              </Link>
              <ul className="submenu-list">
                {visibleSettingsItems.some((item) => item.key === "employees") && (
                  <li>
                    <Link to="/employees">Employees</Link>
                  </li>
                )}
                {visibleSettingsItems.some((item) => item.key === "users") && (
                  <li>
                    <Link to="/users">Users</Link>
                  </li>
                )}
                {visibleSettingsItems.some((item) => item.section) && <li className="submenu-section-label">Management</li>}
                {visibleSettingsItems.some((item) => item.key === "headoffice") && (
                  <li>
                    <Link to="/headoffice">Head Office</Link>
                  </li>
                )}
                {visibleSettingsItems.some((item) => item.key === "branches") && (
                  <li>
                    <Link to="/branches">Branches</Link>
                  </li>
                )}
                {visibleSettingsItems.some((item) => item.key === "departments") && (
                  <li>
                    <Link to="/departments">Departments</Link>
                  </li>
                )}
                {visibleSettingsItems.some((item) => item.key === "designations") && (
                  <li>
                    <Link to="/designations">Designations</Link>
                  </li>
                )}
                {visibleSettingsItems.some((item) => item.key === "teams") && (
                  <li>
                    <Link to="/teams">Teams</Link>
                  </li>
                )}
                {visibleSettingsItems.some((item) => item.key === "role-permissions") && (
                  <li>
                    <Link to="/role-permissions">Role Permissions</Link>
                  </li>
                )}
              </ul>
            </li>
          )}

          {showWorkout && (
            <li onClick={() => handleMenuClick(2)} className={`menu-item ${activeIndex === 2 ? "active" : ""}`}>
              <Link to="#" onClick={(e) => e.preventDefault()}>
                <div className="icon-item">
                  <IconBarbell />
                </div>
                <span>Workout</span>
                <i className="fa fa-angle-right menu-dropwdown"></i>
              </Link>
              <ul className="submenu-list">
                {visibleWorkoutItems.map((item) => (
                  <li key={item.key}>
                    <Link to={item.path}>{item.label}</Link>
                  </li>
                ))}
              </ul>
            </li>
          )}

          {showDiet && (
            <li onClick={() => handleMenuClick(3)} className={`menu-item ${activeIndex === 3 ? "active" : ""}`}>
              <Link to="#" onClick={(e) => e.preventDefault()}>
                <div className="icon-item">
                  <IconCalendar />
                </div>
                <span>Diet Menu</span>
                <i className="fa fa-angle-right menu-dropwdown"></i>
              </Link>
              <ul className="submenu-list">
                {visibleDietItems.map((item) => (
                  <li key={item.key}>
                    <Link to={item.path}>{item.label}</Link>
                  </li>
                ))}
              </ul>
            </li>
          )}

          {showCalendar && (
            <li onClick={() => handleMenuClick(4)} className={`menu-item ${activeIndex === 4 ? "active" : ""}`}>
              <Link to="#" onClick={(e) => e.preventDefault()}>
                <div className="icon-item">
                  <IconCalendarEvent />
                </div>
                <span>Schedule</span>
                <i className="fa fa-angle-right menu-dropwdown"></i>
              </Link>
              <ul className="submenu-list">
                {visibleScheduleItems.map((item) => (
                  <li key={item.key}>
                    <Link to={item.path}>{item.label}</Link>
                  </li>
                ))}
              </ul>
            </li>
          )}

          {showGoals && (
            <li onClick={() => handleMenuClick(5)} className={`menu-item ${activeIndex === 5 ? "active" : ""}`}>
              <Link to="/goals">
                <div className="icon-item">
                  <IconTargetArrow />
                </div>
                <span>Goals</span>
              </Link>
            </li>
          )}

          {showProgress && (
            <li onClick={() => handleMenuClick(6)} className={`menu-item ${activeIndex === 6 ? "active" : ""}`}>
              <Link to="/progress">
                <div className="icon-item">
                  <IconChartBar />
                </div>
                <span>Progress</span>
              </Link>
            </li>
          )}

          {showProfile && (
            <li onClick={() => handleMenuClick(7)} className={`menu-item ${activeIndex === 7 ? "active" : ""}`}>
            <Link to="/profile">
              <div className="icon-item">
                <IconLayoutDashboard />
              </div>
              <span>Profile</span>
            </Link>
            </li>
          )}

          <li onClick={() => handleMenuClick(8)} className={`menu-item ${activeIndex === 8 ? "active" : ""}`}>
            <Link to="#" onClick={(e) => e.preventDefault()}>
              <div className="icon-item">
                <IconKey />
              </div>
              <span>Authentication</span>
              <i className="fa fa-angle-right menu-dropwdown"></i>
            </Link>
            <ul className="submenu-list">
              <li>
                <Link to="/sign-in">Sign In</Link>
              </li>
              <li>
                <Link to="/sign-up">Sign Up</Link>
              </li>
              <li>
                <Link to="/forgot-password">Forgot Password</Link>
              </li>
              <li>
                <Link to="/new-password">reset password</Link>
              </li>
              <li>
                <Link to="/verify-email">verify email</Link>
              </li>
              <li>
                <Link to="/verify-pin">verify pin</Link>
              </li>
            </ul>
          </li>

          {canView("onboding-step") && (
            <li onClick={() => handleMenuClick(9)} className={`menu-item ${activeIndex === 9 ? "active" : ""}`}>
              <Link to="/onboding-step">
                <div className="icon-item">
                  <IconEscalatorDown />
                </div>
                <span>Step</span>
              </Link>
            </li>
          )}
        </ul>
      </SimpleBar>

      <span className={`menu-next ${maxScroll > 0 && isAtEnd ? "disabled" : ""}`} onClick={() => !isAtEnd && handleScroll("right")}>
        <IconChevronRight />
      </span>

      <div className="sidebarpro-sec">
        <img className="img-fluid" src={sidebarvactor} alt="" />
        <h6 className="mb-2 fw-bold">Premium Membership</h6>
        <p>Monitor progress, set goals, and achieve results faster!</p>
        <Link className="btn btn-primary btn-sm" to="#">
          Upgrade
        </Link>
      </div>
    </aside>
  );
}

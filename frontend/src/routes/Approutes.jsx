import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import ProtectedRoute from "../components/ProtectedRoute";

// Public Pages
import Signin from "../pages/auth/Signin";
import Signup from "../pages/auth/Signup";
import Forgotpassword from "../pages/auth/Forgotpassword";
import Newpassword from "../pages/auth/Newpassword";
import Verifyemail from "../pages/auth/Verifyemail";
import Verifypin from "../pages/auth/Verifypin";
import Landing from "../pages/public/Landing";
import Onbodingstep from "../pages/public/Onbodingstep";
import Errorpage from "../pages/public/Errorpage";

// Protected Pages
import Index from "../pages/dashboard/Index";
import Workoutfilter from "../pages/workout/Workoutfilter";
import Workouttopfilter from "../pages/workout/Workouttopfilter";
import Upperbodyworkout from "../pages/workout/Upperbodyworkout";
import Createworkout from "../pages/workout/Createworkout";
import Workoutsummary from "../pages/workout/Workoutsummary";
import WorkoutTypeMaster from "../pages/workout/WorkoutTypeMaster";
import BodyPartMaster from "../pages/workout/BodyPartMaster";
import ExerciseMaster from "../pages/workout/ExerciseMaster";
import WorkoutPlanMaster from "../pages/workout/WorkoutPlanMaster";
import WorkoutPlanDetail from "../pages/workout/WorkoutPlanDetail";
import WorkoutDetail from "../pages/workout/WorkoutDetail";
import TrainerDutySchedule from "../pages/schedule/TrainerDutySchedule";
import UserWorkoutSchedule from "../pages/schedule/UserWorkoutSchedule";
import MySchedule from "../pages/schedule/MySchedule";
import Dietplan from "../pages/diet/Dietplan";
import Dietdetail from "../pages/diet/Dietdetail";
import Goals from "../pages/goals/Goals";
import Schedule from "../pages/schedule/Schedule";
import Progress from "../pages/progress/Progress";
import Profile from "../pages/profile/Profile";

// Users Management Pages
import User from "../pages/usersmanagement/user";
import HeadOffice from "../pages/usersmanagement/headoffice";
import Branch from "../pages/usersmanagement/branch";
import DepartmentPage from "../pages/usersmanagement/department";
import DesignationPage from "../pages/usersmanagement/designation";
import Team from "../pages/usersmanagement/team";
import RolePermissions from "../pages/permissions/RolePermissions";

export default function AppRoutes() {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/sign-in" element={<Signin />} />
      <Route path="/sign-up" element={<Signup />} />
      <Route path="/forgot-password" element={<Forgotpassword />} />
      <Route path="/new-password" element={<Newpassword />} />
      <Route path="/verify-email" element={<Verifyemail />} />
      <Route path="/verify-pin" element={<Verifypin />} />
      <Route path="/landing" element={<Landing />} />
      <Route path="/onboding-step" element={<Onbodingstep />} />
      <Route path="/error-page" element={<Errorpage />} />

      {/* Protected Routes */}
      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<Index />} />
        <Route path="/workout-filter" element={<Workoutfilter />} />
        <Route path="/workout-topfilter" element={<Workouttopfilter />} />
        <Route path="/upperbody-workout" element={<Upperbodyworkout />} />
        <Route path="/create-workout" element={<Createworkout />} />
        <Route path="/workout-summary" element={<Workoutsummary />} />
        <Route path="/workout-type" element={<WorkoutTypeMaster />} />
        <Route path="/body-part" element={<BodyPartMaster />} />
        <Route path="/exercise-master" element={<ExerciseMaster />} />
        <Route path="/workout-plan" element={<WorkoutPlanMaster />} />
        <Route path="/workout-plan/:id" element={<WorkoutPlanDetail />} />
        <Route path="/workout-detail" element={<WorkoutDetail />} />
        <Route path="/workout-detail/:id" element={<WorkoutDetail />} />
        <Route path="/trainer-duty-schedule" element={<TrainerDutySchedule />} />
        <Route path="/user-workout-schedule" element={<UserWorkoutSchedule />} />
        <Route path="/my-schedule" element={<MySchedule />} />
        <Route path="/dietplan" element={<Dietplan />} />
        <Route path="/diet-detail" element={<Dietdetail />} />
        <Route path="/diet-detail/:id" element={<Dietdetail />} />
        <Route path="/goals" element={<Goals />} />
        <Route path="/schedule" element={<Schedule />} />
        <Route path="/progress" element={<Progress />} />
        <Route path="/profile" element={<Profile />} />

        {/* Users Management */}
        <Route path="/employees" element={<User />} />
        <Route path="/users" element={<User />} />
        <Route path="/headoffice" element={<HeadOffice />} />
        <Route path="/branches" element={<Branch />} />
        <Route path="/departments" element={<DepartmentPage />} />
        <Route path="/designations" element={<DesignationPage />} />
        <Route path="/teams" element={<Team />} />
        <Route path="/role-permissions" element={<RolePermissions />} />
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/error-page" replace />} />
    </Routes>
  );
}

import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { IconHome, IconEdit, IconTrash, IconPlus } from '@tabler/icons-react';
import { 
  getAllTeams, 
  getTeamsByDepartment,
  createTeam, 
  updateTeam, 
  deleteTeam,
  getAllDepartments
} from "../../api/orgHierarchyApi";
import { extractApiErrorMessage } from "../../utils/errorMessage";
import { useAuth } from "../../context/AuthContext";

const EMPTY_FORM = {
  name: "",
  departmentId: "",
  description: "",
  teamLead: "",
  memberCount: 0,
  status: "ACTIVE",
};

export default function TeamPage() {
  const { user: currentUser } = useAuth();
  const currentRole = String(currentUser?.role || "").toUpperCase();

  const [rows, setRows] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [deptLoading, setDeptLoading] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [saving, setSaving] = useState(false);

  const [showModal, setShowModal] = useState(false);
  const [isEdit, setIsEdit] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [selectedId, setSelectedId] = useState(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteId, setDeleteId] = useState(null);
  const [modalError, setModalError] = useState("");

  // ── Data loaders ─────────────────────────────────────────────────────────────

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      let data;
      if (currentRole === "SUPER_ADMIN") {
        data = await getAllTeams();
      } else if (currentRole === "ADMIN") {
        data = await getAllTeams(); // Will filter in service layer
      } else if (currentRole === "MANAGER") {
        data = await getAllTeams(); // Will filter by their branch
      } else {
        data = [];
      }
      setRows(Array.isArray(data) ? data : []);
    } catch (e) {
      setRows([]);
      setError(extractApiErrorMessage(e, "Failed to load teams"));
    } finally {
      setLoading(false);
    }
  };

  const loadDepartments = async () => {
    setDeptLoading(true);
    try {
      const data = await getAllDepartments();
      setDepartments(Array.isArray(data) ? data : []);
    } catch (e) {
      setDepartments([]);
    } finally {
      setDeptLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    loadDepartments();
  }, []);

  useEffect(() => {
    if (!notice) return;
    const t = setTimeout(() => setNotice(""), 2200);
    return () => clearTimeout(t);
  }, [notice]);

  // ── Authorization Check ───────────────────────────────────────────────────────

  if (!["SUPER_ADMIN", "ADMIN"].includes(currentRole)) {
    return (
      <div className="content">
        <div className="alert alert-danger">
          ⛔ Only Super Admin and Admin can manage Teams
        </div>
      </div>
    );
  }

  // ── Modal open/close ──────────────────────────────────────────────────────────

  const openAdd = () => {
    setForm(EMPTY_FORM);
    setIsEdit(false);
    setModalError("");
    setShowModal(true);
  };

  const openEdit = (team) => {
    setForm({
      name: team?.name || "",
      departmentId: String(team?.departmentId || ""),
      description: team?.description || "",
      teamLead: team?.teamLead || "",
      memberCount: team?.memberCount || 0,
      status: String(team?.status || "ACTIVE").toUpperCase(),
    });
    setSelectedId(team?.id);
    setIsEdit(true);
    setModalError("");
    setShowModal(true);
  };

  // ── Form submit ───────────────────────────────────────────────────────────────

  const handleSubmit = async (e) => {
    e.preventDefault();
    setModalError("");

    if (!form.name?.trim()) {
      setModalError("Team name is required");
      return;
    }

    if (!form.departmentId) {
      setModalError("Department is required");
      return;
    }

    const payload = {
      name: form.name.trim(),
      departmentId: Number(form.departmentId),
      description: form.description?.trim() || null,
      teamLead: form.teamLead?.trim() || null,
      memberCount: Number(form.memberCount) || 0,
      status: form.status,
    };

    setSaving(true);
    try {
      if (isEdit) {
        await updateTeam(selectedId, payload);
        setNotice("Team updated successfully");
      } else {
        await createTeam(payload);
        setNotice("Team added successfully");
      }
      setShowModal(false);
      setForm(EMPTY_FORM);
      setSelectedId(null);
      await loadData();
    } catch (e) {
      setModalError(extractApiErrorMessage(e, "Operation failed"));
    } finally {
      setSaving(false);
    }
  };

  // ── Delete ────────────────────────────────────────────────────────────────────

  const confirmDelete = (id) => {
    setDeleteId(id);
    setShowDeleteModal(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    setSaving(true);
    setError("");
    try {
      await deleteTeam(deleteId);
      setNotice("Team deleted successfully");
      setShowDeleteModal(false);
      setDeleteId(null);
      await loadData();
    } catch (e) {
      setError(extractApiErrorMessage(e, "Failed to delete team"));
    } finally {
      setSaving(false);
    }
  };

  // ── Statistics ─────────────────────────────────────────────────────────────────

  const totalTeams = rows.length;
  const activeTeams = rows.filter(t => String(t?.status || "").toUpperCase() === "ACTIVE").length;
  const totalMembers = rows.reduce((sum, t) => sum + (Number(t?.memberCount) || 0), 0);

  // ─────────────────────────────────────────────────────────────────────────────

  return (
    <div className="page-wrapper team-page-wrapper">
      <div className="content">
        {notice && <div className="alert alert-success">{notice}</div>}
        {error && <div className="alert alert-danger">{error}</div>}

        <div className="d-md-flex d-block align-items-center justify-content-between page-breadcrumb mb-3">
          <div className="my-auto mb-2">
            <h2 className="mb-1">TEAMS</h2>
            <nav>
              <ol className="breadcrumb mb-0">
                <li className="breadcrumb-item">
                  <Link to="/dashboard">
                    <IconHome size={16} />
                  </Link>
                </li>
                <li className="breadcrumb-item">Organization</li>
                <li className="breadcrumb-item active">Teams</li>
              </ol>
            </nav>
          </div>
          <div className="d-flex gap-2">
            <button className="btn btn-primary" onClick={openAdd}>
              <IconPlus size={16} className="me-2" />
              Add Team
            </button>
          </div>
        </div>

        {/* ── Statistics Cards ── */}
        <div className="row mb-4">
          <div className="col-md-4">
            <div className="card">
              <div className="card-body text-center">
                <h3 className="mb-0">{totalTeams}</h3>
                <p className="text-muted small mb-0">Total Teams</p>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card">
              <div className="card-body text-center">
                <h3 className="mb-0">{activeTeams}</h3>
                <p className="text-muted small mb-0">Active Teams</p>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card">
              <div className="card-body text-center">
                <h3 className="mb-0">{totalMembers}</h3>
                <p className="text-muted small mb-0">Total Members</p>
              </div>
            </div>
          </div>
        </div>

        {/* ── Teams Table ── */}
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center">
            <h5 className="mb-0">Teams List</h5>
          </div>
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-striped table-hover mb-0">
                <thead className="thead-light">
                  <tr>
                    <th>Name</th>
                    <th>Department</th>
                    <th>Team Lead</th>
                    <th>Members</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr>
                      <td colSpan={6} className="text-center py-4">Loading...</td>
                    </tr>
                  ) : rows.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="text-center py-4">No teams found</td>
                    </tr>
                  ) : (
                    rows.map((team) => {
                      const dept = departments.find(d => d.id === team.departmentId);
                      return (
                        <tr key={team.id}>
                          <td className="fw-semibold">{team.name}</td>
                          <td>{dept?.name || "-"}</td>
                          <td>{team.teamLead || "-"}</td>
                          <td>
                            <span className="badge bg-primary">{team.memberCount}</span>
                          </td>
                          <td>
                            <span className={`badge ${team.status === "ACTIVE" ? "bg-success" : "bg-danger"}`}>
                              {team.status === "ACTIVE" ? "Active" : "Inactive"}
                            </span>
                          </td>
                          <td>
                            <button className="btn btn-sm btn-outline-primary me-1" onClick={() => openEdit(team)}>
                              <IconEdit size={14} />
                            </button>
                            <button className="btn btn-sm btn-outline-danger" onClick={() => confirmDelete(team.id)}>
                              <IconTrash size={14} />
                            </button>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* ── Add / Edit Team Modal ─────────────────────────────────────────── */}
        {showModal && (
          <div className="modal fade show" style={{ display: "block" }} tabIndex="-1">
            <div className="modal-dialog modal-lg">
              <form className="modal-content" onSubmit={handleSubmit}>
                <div className="modal-header">
                  <h5 className="modal-title">{isEdit ? "Edit Team" : "Add Team"}</h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)} />
                </div>

                {modalError && (
                  <div className="px-4 pt-3">
                    <div className="alert alert-danger mb-0">{modalError}</div>
                  </div>
                )}

                <div className="modal-body">
                  <div className="row g-3">
                    <div className="col-12">
                      <h6 className="mb-3 text-primary">Team Information</h6>
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Team Name *</label>
                      <input
                        className="form-control"
                        value={form.name}
                        onChange={(e) => setForm({ ...form, name: e.target.value })}
                        required
                      />
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Department *</label>
                      <select
                        className="form-select"
                        value={form.departmentId}
                        onChange={(e) => setForm({ ...form, departmentId: e.target.value })}
                        disabled={deptLoading}
                        required
                      >
                        <option value="">Select Department</option>
                        {departments.map(d => (
                          <option key={d.id} value={d.id}>{d.name}</option>
                        ))}
                      </select>
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Team Lead</label>
                      <input
                        className="form-control"
                        value={form.teamLead}
                        onChange={(e) => setForm({ ...form, teamLead: e.target.value })}
                        placeholder="Team Lead Name"
                      />
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Member Count</label>
                      <input
                        type="number"
                        className="form-control"
                        value={form.memberCount}
                        onChange={(e) => setForm({ ...form, memberCount: e.target.value })}
                        min="0"
                      />
                    </div>

                    <div className="col-12">
                      <label className="form-label">Description</label>
                      <textarea
                        className="form-control"
                        rows={2}
                        value={form.description}
                        onChange={(e) => setForm({ ...form, description: e.target.value })}
                        placeholder="Team description"
                      />
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Status</label>
                      <select
                        className="form-select"
                        value={form.status}
                        onChange={(e) => setForm({ ...form, status: e.target.value })}
                      >
                        <option value="ACTIVE">Active</option>
                        <option value="INACTIVE">Inactive</option>
                      </select>
                    </div>
                  </div>
                </div>

                <div className="modal-footer">
                  <button type="button" className="btn btn-light" onClick={() => setShowModal(false)}>
                    Cancel
                  </button>
                  <button type="submit" className="btn btn-primary" disabled={saving}>
                    {saving ? "Saving..." : "Save Changes"}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}

        {showModal && <div className="modal-backdrop fade show" />}

        {/* ── Delete Confirmation Modal ───────────────────────────────────────────── */}
        {showDeleteModal && (
          <div className="modal fade show" style={{ display: "block" }} tabIndex="-1">
            <div className="modal-dialog modal-dialog-centered">
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title">Confirm Delete</h5>
                  <button type="button" className="btn-close" onClick={() => setShowDeleteModal(false)} />
                </div>
                <div className="modal-body">
                  <p>Are you sure you want to delete this team? This action cannot be undone.</p>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-light" onClick={() => setShowDeleteModal(false)}>
                    Cancel
                  </button>
                  <button type="button" className="btn btn-danger" onClick={handleDelete} disabled={saving}>
                    {saving ? "Deleting..." : "Delete"}
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {showDeleteModal && <div className="modal-backdrop fade show" />}
      </div>
    </div>
  );
}
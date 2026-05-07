import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { IconHome, IconEdit, IconTrash, IconPlus } from "@tabler/icons-react";
import {
  getAllDesignations,
  createDesignation,
  updateDesignation,
  deleteDesignation,
  getAllDepartments,
} from "../../api/orgHierarchyApi";
import { extractApiErrorMessage } from "../../utils/errorMessage";
import { useAuth } from "../../context/AuthContext";

const LEVEL_OPTIONS = ["JUNIOR", "SENIOR", "LEAD", "MANAGER", "HEAD"];

const EMPTY_FORM = {
  name: "",
  departmentId: "",
  description: "",
  level: "JUNIOR",
  salary: 0,
  status: "ACTIVE",
};

export default function DesignationPage() {
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

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      const data = await getAllDesignations();
      setRows(Array.isArray(data) ? data : []);
    } catch (e) {
      setRows([]);
      setError(extractApiErrorMessage(e, "Failed to load designations"));
    } finally {
      setLoading(false);
    }
  };

  const loadDepartments = async () => {
    setDeptLoading(true);
    try {
      const data = await getAllDepartments();
      setDepartments(Array.isArray(data) ? data : []);
    } catch {
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

  if (!["SUPER_ADMIN", "ADMIN"].includes(currentRole)) {
    return (
      <div className="content">
        <div className="alert alert-danger">Only Super Admin and Admin can manage Designations</div>
      </div>
    );
  }

  const openAdd = () => {
    setForm(EMPTY_FORM);
    setIsEdit(false);
    setModalError("");
    setShowModal(true);
  };

  const openEdit = (desig) => {
    setForm({
      name: desig?.name || "",
      departmentId: String(desig?.departmentId || ""),
      description: desig?.description || "",
      level: desig?.level || "JUNIOR",
      salary: desig?.salary || 0,
      status: String(desig?.status || "ACTIVE").toUpperCase(),
    });
    setSelectedId(desig?.id);
    setIsEdit(true);
    setModalError("");
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setModalError("");

    if (!form.name?.trim()) return setModalError("Designation name is required");
    if (!form.departmentId) return setModalError("Department is required");

    const payload = {
      name: form.name.trim(),
      departmentId: Number(form.departmentId),
      description: form.description?.trim() || null,
      level: form.level,
      salary: Number(form.salary) || 0,
      status: form.status,
    };

    setSaving(true);
    try {
      if (isEdit) {
        await updateDesignation(selectedId, payload);
        setNotice("Designation updated successfully");
      } else {
        await createDesignation(payload);
        setNotice("Designation added successfully");
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

  const confirmDelete = (id) => {
    setDeleteId(id);
    setShowDeleteModal(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    setSaving(true);
    setError("");
    try {
      await deleteDesignation(deleteId);
      setNotice("Designation deleted successfully");
      setShowDeleteModal(false);
      setDeleteId(null);
      await loadData();
    } catch (e) {
      setError(extractApiErrorMessage(e, "Failed to delete designation"));
    } finally {
      setSaving(false);
    }
  };

  const totalDesignations = rows.length;
  const activeDesignations = rows.filter((d) => String(d?.status || "").toUpperCase() === "ACTIVE").length;

  return (
    <div className="page-wrapper designation-page-wrapper">
      <div className="content">
        {notice && <div className="alert alert-success">{notice}</div>}
        {error && <div className="alert alert-danger">{error}</div>}

        <div className="d-md-flex d-block align-items-center justify-content-between page-breadcrumb mb-3">
          <div className="my-auto mb-2">
            <h2 className="mb-1">DESIGNATIONS</h2>
            <nav>
              <ol className="breadcrumb mb-0">
                <li className="breadcrumb-item">
                  <Link to="/">
                    <IconHome size={16} />
                  </Link>
                </li>
                <li className="breadcrumb-item">Organization</li>
                <li className="breadcrumb-item active">Designations</li>
              </ol>
            </nav>
          </div>
          <button className="btn btn-primary" onClick={openAdd}>
            <IconPlus size={16} className="me-2" />
            Add Designation
          </button>
        </div>

        <div className="row mb-4">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body text-center">
                <h3 className="mb-0">{totalDesignations}</h3>
                <p className="text-muted small mb-0">Total Designations</p>
              </div>
            </div>
          </div>
          <div className="col-md-6">
            <div className="card">
              <div className="card-body text-center">
                <h3 className="mb-0">{activeDesignations}</h3>
                <p className="text-muted small mb-0">Active Designations</p>
              </div>
            </div>
          </div>
        </div>

        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center">
            <h5 className="mb-0">Designations List</h5>
          </div>
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-striped table-hover mb-0">
                <thead className="thead-light">
                  <tr>
                    <th>Name</th>
                    <th>Department</th>
                    <th>Level</th>
                    <th>Salary</th>
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
                      <td colSpan={6} className="text-center py-4">No designations found</td>
                    </tr>
                  ) : (
                    rows.map((desig) => {
                      const dept = departments.find((d) => d.id === desig.departmentId);
                      return (
                        <tr key={desig.id}>
                          <td className="fw-semibold">{desig.name}</td>
                          <td>{dept?.name || "-"}</td>
                          <td>
                            <span className="badge bg-info">{desig.level}</span>
                          </td>
                          <td>₹ {Number(desig.salary || 0).toLocaleString("en-IN")}</td>
                          <td>
                            <span className={`badge ${desig.status === "ACTIVE" ? "bg-success" : "bg-danger"}`}>
                              {desig.status === "ACTIVE" ? "Active" : "Inactive"}
                            </span>
                          </td>
                          <td>
                            <button className="btn btn-sm btn-outline-primary me-1" onClick={() => openEdit(desig)}>
                              <IconEdit size={14} />
                            </button>
                            <button className="btn btn-sm btn-outline-danger" onClick={() => confirmDelete(desig.id)}>
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

        {showModal && (
          <div className="modal fade show" style={{ display: "block" }} tabIndex="-1">
            <div className="modal-dialog modal-lg">
              <form className="modal-content" onSubmit={handleSubmit}>
                <div className="modal-header">
                  <h5 className="modal-title">{isEdit ? "Edit Designation" : "Add Designation"}</h5>
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
                      <h6 className="mb-3 text-primary">Designation Information</h6>
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Designation Name *</label>
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
                        {departments.map((d) => (
                          <option key={d.id} value={d.id}>
                            {d.name}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Level</label>
                      <select
                        className="form-select"
                        value={form.level}
                        onChange={(e) => setForm({ ...form, level: e.target.value })}
                      >
                        {LEVEL_OPTIONS.map((level) => (
                          <option key={level} value={level}>
                            {level}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Salary (₹)</label>
                      <input
                        type="number"
                        className="form-control"
                        value={form.salary}
                        onChange={(e) => setForm({ ...form, salary: e.target.value })}
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

        {showDeleteModal && (
          <div className="modal fade show" style={{ display: "block" }} tabIndex="-1">
            <div className="modal-dialog modal-dialog-centered">
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title">Confirm Delete</h5>
                  <button type="button" className="btn-close" onClick={() => setShowDeleteModal(false)} />
                </div>
                <div className="modal-body">
                  <p>Are you sure you want to delete this designation? This action cannot be undone.</p>
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

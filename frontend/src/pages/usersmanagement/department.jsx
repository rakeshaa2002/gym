import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { IconHome, IconEdit, IconTrash, IconPlus } from "@tabler/icons-react";
import {
  getAllDepartments,
  getDepartmentsByBranch,
  createDepartment,
  updateDepartment,
  deleteDepartment,
  getAllBranches,
} from "../../api/orgHierarchyApi";
import { extractApiErrorMessage } from "../../utils/errorMessage";
import { useAuth } from "../../context/AuthContext";

const EMPTY_FORM = {
  name: "",
  branchId: "",
  description: "",
  status: "ACTIVE",
};

export default function DepartmentPage() {
  const { user: currentUser } = useAuth();
  const currentRole = String(currentUser?.role || "").toUpperCase();

  const [rows, setRows] = useState([]);
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(false);
  const [brLoading, setBrLoading] = useState(false);
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
      let data;
      if (currentRole === "SUPER_ADMIN") data = await getAllDepartments();
      else if (currentRole === "ADMIN") data = await getDepartmentsByBranch(currentUser?.branchId);
      else data = [];
      setRows(Array.isArray(data) ? data : []);
    } catch (e) {
      setRows([]);
      setError(extractApiErrorMessage(e, "Failed to load departments"));
    } finally {
      setLoading(false);
    }
  };

  const loadBranches = async () => {
    setBrLoading(true);
    try {
      const data = await getAllBranches();
      setBranches(Array.isArray(data) ? data : []);
    } catch {
      setBranches([]);
    } finally {
      setBrLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    loadBranches();
  }, []);

  useEffect(() => {
    if (!notice) return;
    const t = setTimeout(() => setNotice(""), 2200);
    return () => clearTimeout(t);
  }, [notice]);

  if (!["SUPER_ADMIN", "ADMIN"].includes(currentRole)) {
    return (
      <div className="content">
        <div className="alert alert-danger">Only Super Admin and Admin can manage Departments</div>
      </div>
    );
  }

  const openAdd = () => {
    setForm({ ...EMPTY_FORM, branchId: currentRole === "ADMIN" ? String(currentUser?.branchId) : "" });
    setIsEdit(false);
    setModalError("");
    setShowModal(true);
  };

  const openEdit = (dept) => {
    setForm({
      name: dept?.name || "",
      branchId: String(dept?.branchId || ""),
      description: dept?.description || "",
      status: String(dept?.status || "ACTIVE").toUpperCase(),
    });
    setSelectedId(dept?.id);
    setIsEdit(true);
    setModalError("");
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setModalError("");
    if (!form.name?.trim()) return setModalError("Department name is required");
    if (!form.branchId) return setModalError("Branch is required");

    const payload = {
      name: form.name.trim(),
      branchId: Number(form.branchId),
      description: form.description?.trim() || null,
      status: form.status,
    };

    setSaving(true);
    try {
      if (isEdit) {
        await updateDepartment(selectedId, payload);
        setNotice("Department updated successfully");
      } else {
        await createDepartment(payload);
        setNotice("Department added successfully");
      }
      setShowModal(false);
      setForm(EMPTY_FORM);
      setSelectedId(null);
      await loadData();
    } catch (e2) {
      setModalError(extractApiErrorMessage(e2, "Operation failed"));
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
      await deleteDepartment(deleteId);
      setNotice("Department deleted successfully");
      setShowDeleteModal(false);
      setDeleteId(null);
      await loadData();
    } catch (e) {
      setError(extractApiErrorMessage(e, "Failed to delete department"));
    } finally {
      setSaving(false);
    }
  };

  const totalDepartments = rows.length;
  const activeDepartments = rows.filter((d) => String(d?.status || "").toUpperCase() === "ACTIVE").length;

  return (
    <div className="page-wrapper department-page-wrapper">
      <div className="content">
        {notice && <div className="alert alert-success">{notice}</div>}
        {error && <div className="alert alert-danger">{error}</div>}

        <div className="d-md-flex d-block align-items-center justify-content-between page-breadcrumb mb-3">
          <div className="my-auto mb-2">
            <h2 className="mb-1">DEPARTMENTS</h2>
            <nav>
              <ol className="breadcrumb mb-0">
                <li className="breadcrumb-item">
                  <Link to="/"><IconHome size={16} /></Link>
                </li>
                <li className="breadcrumb-item">Organization</li>
                <li className="breadcrumb-item active">Departments</li>
              </ol>
            </nav>
          </div>
          <button className="btn btn-primary" onClick={openAdd}>
            <IconPlus size={16} className="me-2" />Add Department
          </button>
        </div>

        <div className="row mb-4">
          <div className="col-md-6"><div className="card"><div className="card-body text-center"><h3 className="mb-0">{totalDepartments}</h3><p className="text-muted small mb-0">Total Departments</p></div></div></div>
          <div className="col-md-6"><div className="card"><div className="card-body text-center"><h3 className="mb-0">{activeDepartments}</h3><p className="text-muted small mb-0">Active Departments</p></div></div></div>
        </div>

        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center">
            <h5 className="mb-0">Departments List</h5>
          </div>
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-striped table-hover mb-0">
                <thead className="thead-light">
                  <tr><th>Name</th><th>Branch</th><th>Description</th><th>Status</th><th>Actions</th></tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr><td colSpan={5} className="text-center py-4">Loading...</td></tr>
                  ) : rows.length === 0 ? (
                    <tr><td colSpan={5} className="text-center py-4">No departments found</td></tr>
                  ) : rows.map((dept) => {
                    const branch = branches.find((b) => b.id === dept.branchId);
                    return (
                      <tr key={dept.id}>
                        <td className="fw-semibold">{dept.name}</td>
                        <td>{branch?.name || "-"}</td>
                        <td>{dept.description || "-"}</td>
                        <td><span className={`badge ${dept.status === "ACTIVE" ? "bg-success" : "bg-danger"}`}>{dept.status === "ACTIVE" ? "Active" : "Inactive"}</span></td>
                        <td>
                          <button className="btn btn-sm btn-outline-primary me-1" onClick={() => openEdit(dept)}><IconEdit size={14} /></button>
                          <button className="btn btn-sm btn-outline-danger" onClick={() => confirmDelete(dept.id)}><IconTrash size={14} /></button>
                        </td>
                      </tr>
                    );
                  })}
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
                  <h5 className="modal-title">{isEdit ? "Edit Department" : "Add Department"}</h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)} />
                </div>

                {modalError && <div className="px-4 pt-3"><div className="alert alert-danger mb-0">{modalError}</div></div>}

                <div className="modal-body">
                  <div className="row g-3">
                    <div className="col-12"><h6 className="mb-3 text-primary">Department Information</h6></div>

                    <div className="col-md-6">
                      <label className="form-label">Department Name *</label>
                      <input className="form-control" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Branch *</label>
                      <select
                        className="form-select"
                        value={form.branchId}
                        onChange={(e) => setForm({ ...form, branchId: e.target.value })}
                        disabled={currentRole === "ADMIN" || brLoading}
                        required
                      >
                        <option value="">Select Branch</option>
                        {branches.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
                      </select>
                    </div>

                    <div className="col-12">
                      <label className="form-label">Description</label>
                      <textarea className="form-control" rows={2} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Status</label>
                      <select className="form-select" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                        <option value="ACTIVE">Active</option>
                        <option value="INACTIVE">Inactive</option>
                      </select>
                    </div>
                  </div>
                </div>

                <div className="modal-footer">
                  <button type="button" className="btn btn-light" onClick={() => setShowModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? "Saving..." : "Save Changes"}</button>
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
                <div className="modal-body"><p>Are you sure you want to delete this department? This action cannot be undone.</p></div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-light" onClick={() => setShowDeleteModal(false)}>Cancel</button>
                  <button type="button" className="btn btn-danger" onClick={handleDelete} disabled={saving}>{saving ? "Deleting..." : "Delete"}</button>
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

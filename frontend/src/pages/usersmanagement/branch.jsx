import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { IconHome, IconEdit, IconTrash, IconPlus } from '@tabler/icons-react';
import { 
  getAllBranches, 
  getBranchesByHeadOffice,
  createBranch, 
  updateBranch, 
  deleteBranch,
  getAllHeadOffices 
} from "../../api/orgHierarchyApi";
import { extractApiErrorMessage } from "../../utils/errorMessage";
import api from "../../utils/api";
import { useAuth } from "../../context/AuthContext";


function getCurrentUserId(user) {
  const raw = user?.userId ?? user?.id ?? localStorage.getItem("userId");
  const parsed = Number(raw);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}
const EMPTY_FORM = {
  name: "",
  headOfficeId: "",
  location: "",
  address: "",
  phone: "",
  email: "",
  managerName: "",
  status: "ACTIVE",
};

export default function BranchPage() {
  const { user: currentUser } = useAuth();
  const currentRole = String(currentUser?.role || "").toUpperCase();
  const currentUserId = getCurrentUserId(currentUser);

  const [rows, setRows] = useState([]);
  const [headOffices, setHeadOffices] = useState([]);
  const [managerOptions, setManagerOptions] = useState([]);
  const [managerLoading, setManagerLoading] = useState(false);
  const [loading, setLoading] = useState(false);
  const [heLoading, setHeLoading] = useState(false);
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

  // â”€â”€ Data loaders â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  const loadData = async () => {
    setLoading(true);
    setError("");
    try {
      let data;
      if (currentRole === "SUPER_ADMIN") {
        data = await getAllBranches();
      } else if (currentRole === "ADMIN") {
        data = await getBranchesByHeadOffice(currentUser?.headOfficeId);
      } else {
        data = [];
      }
      setRows(Array.isArray(data) ? data : []);
    } catch (e) {
      setRows([]);
      setError(extractApiErrorMessage(e, "Failed to load branches"));
    } finally {
      setLoading(false);
    }
  };

  const loadHeadOffices = async () => {
    setHeLoading(true);
    try {
      let data;
      if (currentRole === "SUPER_ADMIN") {
        data = await getAllHeadOffices();
      } else if (currentRole === "ADMIN") {
        data = [{ id: currentUser?.headOfficeId, name: currentUser?.headOfficeName || "My Office" }];
      } else {
        data = [];
      }
      setHeadOffices(Array.isArray(data) ? data : []);
    } catch (e) {
      setHeadOffices([]);
    } finally {
      setHeLoading(false);
    }
  };


  const loadManagers = async () => {
    if (!currentUserId) return;
    setManagerLoading(true);
    try {
      const response = await api.get("/users/managers", { params: { requesterId: currentUserId } });
      const data = response?.data?.data;
      setManagerOptions(Array.isArray(data) ? data : []);
    } catch (e) {
      setManagerOptions([]);
    } finally {
      setManagerLoading(false);
    }
  };
  useEffect(() => {
    loadData();
    loadHeadOffices();
    loadManagers();
  }, []);

  useEffect(() => {
    if (!notice) return;
    const t = setTimeout(() => setNotice(""), 2200);
    return () => clearTimeout(t);
  }, [notice]);

  // â”€â”€ Authorization Check â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  if (!["SUPER_ADMIN", "ADMIN"].includes(currentRole)) {
    return (
      <div className="content">
        <div className="alert alert-danger">
          â›” Only Super Admin and Admin can manage Branches
        </div>
      </div>
    );
  }

  // â”€â”€ Modal open/close â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  const openAdd = () => {
    setForm({
      ...EMPTY_FORM,
      headOfficeId: currentRole === "ADMIN" ? String(currentUser?.headOfficeId) : "",
    });
    setIsEdit(false);
    setModalError("");
    setShowModal(true);
  };

  const openEdit = (branch) => {
    setForm({
      name: branch?.name || "",
      headOfficeId: String(branch?.headOfficeId || ""),
      location: branch?.location || "",
      address: branch?.address || "",
      phone: branch?.phone || "",
      email: branch?.email || "",
      managerName: branch?.managerName || "",
      status: String(branch?.status || "ACTIVE").toUpperCase(),
    });
    setSelectedId(branch?.id);
    setIsEdit(true);
    setModalError("");
    setShowModal(true);
  };

  // â”€â”€ Form submit â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  const handleSubmit = async (e) => {
    e.preventDefault();
    setModalError("");

    if (!form.name?.trim()) {
      setModalError("Branch name is required");
      return;
    }

    if (!form.headOfficeId) {
      setModalError("Head office is required");
      return;
    }

    const payload = {
      name: form.name.trim(),
      headOfficeId: Number(form.headOfficeId),
      location: form.location?.trim() || null,
      address: form.address?.trim() || null,
      phone: form.phone?.trim() || null,
      email: form.email?.trim() || null,
      managerName: form.managerName?.trim() || null,
      status: form.status,
    };

    setSaving(true);
    try {
      if (isEdit) {
        await updateBranch(selectedId, payload);
        setNotice("Branch updated successfully");
      } else {
        await createBranch(payload);
        setNotice("Branch added successfully");
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

  // â”€â”€ Delete â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  const confirmDelete = (id) => {
    setDeleteId(id);
    setShowDeleteModal(true);
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    setSaving(true);
    setError("");
    try {
      await deleteBranch(deleteId);
      setNotice("Branch deleted successfully");
      setShowDeleteModal(false);
      setDeleteId(null);
      await loadData();
    } catch (e) {
      setError(extractApiErrorMessage(e, "Failed to delete branch"));
    } finally {
      setSaving(false);
    }
  };

  // â”€â”€ Statistics â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€


  const filteredManagers = managerOptions.filter((manager) => {
    const managerHeadOfficeId = manager?.headOfficeId ?? manager?.headOffice?.id;
    if (!form.headOfficeId) return true;
    return String(managerHeadOfficeId || "") === String(form.headOfficeId);
  });
  const totalBranches = rows.length;
  const activeBranches = rows.filter(b => String(b?.status || "").toUpperCase() === "ACTIVE").length;

  // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  return (
    <div className="page-wrapper branch-page-wrapper">
      <div className="content">
        {notice && <div className="alert alert-success">{notice}</div>}
        {error && <div className="alert alert-danger">{error}</div>}

        <div className="d-md-flex d-block align-items-center justify-content-between page-breadcrumb mb-3">
          <div className="my-auto mb-2">
            <h2 className="mb-1">BRANCHES</h2>
            <nav>
              <ol className="breadcrumb mb-0">
                <li className="breadcrumb-item">
                  <Link to="/dashboard">
                    <IconHome size={16} />
                  </Link>
                </li>
                <li className="breadcrumb-item">Organization</li>
                <li className="breadcrumb-item active">Branches</li>
              </ol>
            </nav>
          </div>
          <div className="d-flex gap-2">
            <button className="btn btn-primary" onClick={openAdd}>
              <IconPlus size={16} className="me-2" />
              Add Branch
            </button>
          </div>
        </div>

        {/* â”€â”€ Statistics Cards â”€â”€ */}
        <div className="row mb-4">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body text-center">
                <h3 className="mb-0">{totalBranches}</h3>
                <p className="text-muted small mb-0">Total Branches</p>
              </div>
            </div>
          </div>
          <div className="col-md-6">
            <div className="card">
              <div className="card-body text-center">
                <h3 className="mb-0">{activeBranches}</h3>
                <p className="text-muted small mb-0">Active Branches</p>
              </div>
            </div>
          </div>
        </div>

        {/* â”€â”€ Branches Table â”€â”€ */}
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center">
            <h5 className="mb-0">Branches List</h5>
          </div>
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-striped table-hover mb-0">
                <thead className="thead-light">
                  <tr>
                    <th>Name</th>
                    <th>Head Office</th>
                    <th>Location</th>
                    <th>Manager</th>
                    <th>Contact</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr>
                      <td colSpan={7} className="text-center py-4">Loading...</td>
                    </tr>
                  ) : rows.length === 0 ? (
                    <tr>
                      <td colSpan={7} className="text-center py-4">No branches found</td>
                    </tr>
                  ) : (
                    rows.map((branch) => {
                      const headOffice = headOffices.find(h => h.id === branch.headOfficeId);
                      return (
                        <tr key={branch.id}>
                          <td className="fw-semibold">{branch.name}</td>
                          <td>{headOffice?.name || "-"}</td>
                          <td>{branch.location || "-"}</td>
                          <td>{branch.managerName || "-"}</td>
                          <td>{branch.phone || "-"}</td>
                          <td>
                            <span className={`badge ${branch.status === "ACTIVE" ? "bg-success" : "bg-danger"}`}>
                              {branch.status === "ACTIVE" ? "Active" : "Inactive"}
                            </span>
                          </td>
                          <td>
                            <button className="btn btn-sm btn-outline-primary me-1" onClick={() => openEdit(branch)}>
                              <IconEdit size={14} />
                            </button>
                            <button className="btn btn-sm btn-outline-danger" onClick={() => confirmDelete(branch.id)}>
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

        {/* â”€â”€ Add / Edit Branch Modal â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */}
        {showModal && (
          <div className="modal fade show" style={{ display: "block" }} tabIndex="-1">
            <div className="modal-dialog modal-lg">
              <form className="modal-content" onSubmit={handleSubmit}>
                <div className="modal-header">
                  <h5 className="modal-title">{isEdit ? "Edit Branch" : "Add Branch"}</h5>
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
                      <h6 className="mb-3 text-primary">Branch Information</h6>
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Branch Name *</label>
                      <input
                        className="form-control"
                        value={form.name}
                        onChange={(e) => setForm({ ...form, name: e.target.value })}
                        required
                      />
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Head Office *</label>
                      <select
                        className="form-select"
                        value={form.headOfficeId}
                        onChange={(e) => setForm({ ...form, headOfficeId: e.target.value })}
                        disabled={currentRole === "ADMIN" || heLoading}
                        required
                      >
                        <option value="">Select Head Office</option>
                        {headOffices.map(office => (
                          <option key={office.id} value={office.id}>{office.name}</option>
                        ))}
                      </select>
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Location</label>
                      <input
                        className="form-control"
                        value={form.location}
                        onChange={(e) => setForm({ ...form, location: e.target.value })}
                        placeholder="e.g. Bangalore"
                      />
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Manager Name</label>
                      <input
                        className="form-control"
                        value={form.managerName}
                        onChange={(e) => setForm({ ...form, managerName: e.target.value })}
                        placeholder="Branch Manager"
                      />
                    </div>

                    <div className="col-12">
                      <label className="form-label">Address</label>
                      <textarea
                        className="form-control"
                        rows={2}
                        value={form.address}
                        onChange={(e) => setForm({ ...form, address: e.target.value })}
                        placeholder="Full address"
                      />
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Phone</label>
                      <input
                        className="form-control"
                        value={form.phone}
                        onChange={(e) => setForm({ ...form, phone: e.target.value })}
                        placeholder="+91 XXXXXXXXXX"
                      />
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Email</label>
                      <input
                        type="email"
                        className="form-control"
                        value={form.email}
                        onChange={(e) => setForm({ ...form, email: e.target.value })}
                        placeholder="branch@example.com"
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

        {/* â”€â”€ Delete Confirmation Modal â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */}
        {showDeleteModal && (
          <div className="modal fade show" style={{ display: "block" }} tabIndex="-1">
            <div className="modal-dialog modal-dialog-centered">
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title">Confirm Delete</h5>
                  <button type="button" className="btn-close" onClick={() => setShowDeleteModal(false)} />
                </div>
                <div className="modal-body">
                  <p>Are you sure you want to delete this branch? This action cannot be undone.</p>
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

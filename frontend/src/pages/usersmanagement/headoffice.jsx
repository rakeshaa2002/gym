import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { IconHome, IconEdit, IconTrash, IconPlus } from '@tabler/icons-react';
import { getAllHeadOffices, createHeadOffice, updateHeadOffice, deleteHeadOffice } from "../../api/orgHierarchyApi";
import { extractApiErrorMessage } from "../../utils/errorMessage";
import { useAuth } from "../../context/AuthContext";

const EMPTY_FORM = {
  name: "",
  location: "",
  address: "",
  phone: "",
  email: "",
  status: "ACTIVE",
};

export default function HeadOfficePage() {
  const { user: currentUser } = useAuth();
  const currentRole = String(currentUser?.role || "").toUpperCase();

  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
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
      const data = await getAllHeadOffices();
      setRows(Array.isArray(data) ? data : []);
    } catch (e) {
      setRows([]);
      setError(extractApiErrorMessage(e, "Failed to load head offices"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    if (!notice) return;
    const t = setTimeout(() => setNotice(""), 2200);
    return () => clearTimeout(t);
  }, [notice]);

  // ── Authorization Check ───────────────────────────────────────────────────────

  if (currentRole !== "SUPER_ADMIN") {
    return (
      <div className="content">
        <div className="alert alert-danger">
          ⛔ Only Super Admin can manage Head Offices
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

  const openEdit = (office) => {
    setForm({
      name: office?.name || "",
      location: office?.location || "",
      address: office?.address || "",
      phone: office?.phone || "",
      email: office?.email || "",
      status: String(office?.status || "ACTIVE").toUpperCase(),
    });
    setSelectedId(office?.id);
    setIsEdit(true);
    setModalError("");
    setShowModal(true);
  };

  // ── Form submit ───────────────────────────────────────────────────────────────

  const handleSubmit = async (e) => {
    e.preventDefault();
    setModalError("");

    if (!form.name?.trim()) {
      setModalError("Head office name is required");
      return;
    }

    const payload = {
      name: form.name.trim(),
      location: form.location?.trim() || null,
      address: form.address?.trim() || null,
      phone: form.phone?.trim() || null,
      email: form.email?.trim() || null,
      status: form.status,
    };

    setSaving(true);
    try {
      if (isEdit) {
        await updateHeadOffice(selectedId, payload);
        setNotice("Head office updated successfully");
      } else {
        await createHeadOffice(payload);
        setNotice("Head office added successfully");
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
      await deleteHeadOffice(deleteId);
      setNotice("Head office deleted successfully");
      setShowDeleteModal(false);
      setDeleteId(null);
      await loadData();
    } catch (e) {
      setError(extractApiErrorMessage(e, "Failed to delete head office"));
    } finally {
      setSaving(false);
    }
  };

  // ── Statistics ─────────────────────────────────────────────────────────────────

  const totalHeadOffices = rows.length;
  const activeHeadOffices = rows.filter(h => String(h?.status || "").toUpperCase() === "ACTIVE").length;

  // ─────────────────────────────────────────────────────────────────────────────

  return (
    <div className="page-wrapper head-office-page-wrapper">
      <div className="content">
        {notice && <div className="alert alert-success">{notice}</div>}
        {error && <div className="alert alert-danger">{error}</div>}

        <div className="d-md-flex d-block align-items-center justify-content-between page-breadcrumb mb-3">
          <div className="my-auto mb-2">
            <h2 className="mb-1">HEAD OFFICES</h2>
            <nav>
              <ol className="breadcrumb mb-0">
                <li className="breadcrumb-item">
                  <Link to="/dashboard">
                    <IconHome size={16} />
                  </Link>
                </li>
                <li className="breadcrumb-item">Organization</li>
                <li className="breadcrumb-item active">Head Offices</li>
              </ol>
            </nav>
          </div>
          <div className="d-flex gap-2">
            <button className="btn btn-primary" onClick={openAdd}>
              <IconPlus size={16} className="me-2" />
              Add Head Office
            </button>
          </div>
        </div>

        {/* ── Statistics Cards ── */}
        <div className="row mb-4">
          <div className="col-md-6">
            <div className="card">
              <div className="card-body text-center">
                <h3 className="mb-0">{totalHeadOffices}</h3>
                <p className="text-muted small mb-0">Total Head Offices</p>
              </div>
            </div>
          </div>
          <div className="col-md-6">
            <div className="card">
              <div className="card-body text-center">
                <h3 className="mb-0">{activeHeadOffices}</h3>
                <p className="text-muted small mb-0">Active Head Offices</p>
              </div>
            </div>
          </div>
        </div>

        {/* ── Head Offices Table ── */}
        <div className="card">
          <div className="card-header d-flex justify-content-between align-items-center">
            <h5 className="mb-0">Head Offices List</h5>
          </div>
          <div className="card-body p-0">
            <div className="table-responsive">
              <table className="table table-striped table-hover mb-0">
                <thead className="thead-light">
                  <tr>
                    <th>Name</th>
                    <th>Location</th>
                    <th>Contact</th>
                    <th>Email</th>
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
                      <td colSpan={6} className="text-center py-4">No head offices found</td>
                    </tr>
                  ) : (
                    rows.map((office) => (
                      <tr key={office.id}>
                        <td className="fw-semibold">{office.name}</td>
                        <td>{office.location || "-"}</td>
                        <td>{office.phone || "-"}</td>
                        <td>{office.email || "-"}</td>
                        <td>
                          <span className={`badge ${office.status === "ACTIVE" ? "bg-success" : "bg-danger"}`}>
                            {office.status === "ACTIVE" ? "Active" : "Inactive"}
                          </span>
                        </td>
                        <td>
                          <button className="btn btn-sm btn-outline-primary me-1" onClick={() => openEdit(office)}>
                            <IconEdit size={14} />
                          </button>
                          <button className="btn btn-sm btn-outline-danger" onClick={() => confirmDelete(office.id)}>
                            <IconTrash size={14} />
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* ── Add / Edit Head Office Modal ─────────────────────────────────────────── */}
        {showModal && (
          <div className="modal fade show" style={{ display: "block" }} tabIndex="-1">
            <div className="modal-dialog modal-lg">
              <form className="modal-content" onSubmit={handleSubmit}>
                <div className="modal-header">
                  <h5 className="modal-title">{isEdit ? "Edit Head Office" : "Add Head Office"}</h5>
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
                      <h6 className="mb-3 text-primary">Basic Information</h6>
                    </div>

                    <div className="col-md-6">
                      <label className="form-label">Name *</label>
                      <input
                        className="form-control"
                        value={form.name}
                        onChange={(e) => setForm({ ...form, name: e.target.value })}
                        required
                      />
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
                        placeholder="office@example.com"
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
                  <p>Are you sure you want to delete this head office? This action cannot be undone.</p>
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
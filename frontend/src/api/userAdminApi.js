import api from "../utils/api";

const unwrap = (res) => res?.data?.data;

export async function getTrainers(requesterId) {
  const res = await api.get("/users/trainers", { params: { requesterId } });
  return unwrap(res) || [];
}

export async function getTrainer(trainerId, requesterId) {
  const res = await api.get(`/users/trainer/${trainerId}`, { params: { requesterId } });
  return unwrap(res) || null;
}

export async function createTrainer(payload, creatorId) {
  const res = await api.post("/users/trainer", payload, { params: { creatorId } });
  return unwrap(res) || null;
}

export async function updateTrainer(trainerId, payload, updaterId) {
  const res = await api.put(`/users/trainer/${trainerId}`, payload, { params: { updaterId } });
  return unwrap(res) || null;
}

export async function deleteTrainer(trainerId, deleterId) {
  await api.delete(`/users/trainer/${trainerId}`, { params: { deleterId } });
  return true;
}

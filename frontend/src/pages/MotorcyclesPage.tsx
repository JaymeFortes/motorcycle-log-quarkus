import { useEffect, useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { createMotorcycle, deleteMotorcycle, listMotorcycles, updateMotorcycle } from "../api/motorcycles";
import { ApiError } from "../api/client";
import type { CreateMotorcycleRequest, MotorcycleResponse } from "../types/dtos";

const EMPTY_FORM: CreateMotorcycleRequest = {
  brand: "",
  model: "",
  modelYear: new Date().getFullYear(),
  plate: "",
  currentKm: 0,
  currentEngineHours: 0,
};

export function MotorcyclesPage() {
  const [motorcycles, setMotorcycles] = useState<MotorcycleResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<CreateMotorcycleRequest>(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  async function reload() {
    setLoading(true);
    setError(null);
    try {
      setMotorcycles(await listMotorcycles());
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Nao foi possivel carregar as motos.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    reload();
  }, []);

  function openCreateForm() {
    setEditingId(null);
    setForm(EMPTY_FORM);
    setFormError(null);
    setShowForm(true);
  }

  function openEditForm(motorcycle: MotorcycleResponse) {
    setEditingId(motorcycle.id);
    setForm({
      brand: motorcycle.brand,
      model: motorcycle.model,
      modelYear: motorcycle.modelYear,
      plate: motorcycle.plate,
      currentKm: motorcycle.currentKm,
      currentEngineHours: motorcycle.currentEngineHours,
    });
    setFormError(null);
    setShowForm(true);
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    setFormError(null);
    try {
      if (editingId !== null) {
        await updateMotorcycle(editingId, form);
      } else {
        await createMotorcycle(form);
      }
      setShowForm(false);
      await reload();
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : "Nao foi possivel salvar a moto.");
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(id: number) {
    if (!confirm("Excluir essa moto? Essa acao nao pode ser desfeita.")) {
      return;
    }
    try {
      await deleteMotorcycle(id);
      await reload();
    } catch (err) {
      alert(err instanceof ApiError ? err.message : "Nao foi possivel excluir a moto.");
    }
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-slate-900">Minhas motos</h1>
        <button
          onClick={openCreateForm}
          className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white hover:bg-slate-800"
        >
          + Nova moto
        </button>
      </div>

      {showForm && (
        <form
          onSubmit={handleSubmit}
          className="mb-8 grid grid-cols-2 gap-4 rounded-lg border border-slate-200 bg-slate-50 p-4"
        >
          <h2 className="col-span-2 text-lg font-medium text-slate-900">
            {editingId !== null ? "Editar moto" : "Nova moto"}
          </h2>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Marca</label>
            <input
              type="text"
              required
              value={form.brand}
              onChange={(e) => setForm({ ...form, brand: e.target.value })}
              className="w-full rounded-md border border-slate-300 px-3 py-2"
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Modelo</label>
            <input
              type="text"
              required
              value={form.model}
              onChange={(e) => setForm({ ...form, model: e.target.value })}
              className="w-full rounded-md border border-slate-300 px-3 py-2"
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Ano</label>
            <input
              type="number"
              required
              value={form.modelYear}
              onChange={(e) => setForm({ ...form, modelYear: Number(e.target.value) })}
              className="w-full rounded-md border border-slate-300 px-3 py-2"
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Placa</label>
            <input
              type="text"
              required
              value={form.plate}
              onChange={(e) => setForm({ ...form, plate: e.target.value })}
              className="w-full rounded-md border border-slate-300 px-3 py-2"
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Km atual</label>
            <input
              type="number"
              required
              min={0}
              value={form.currentKm}
              onChange={(e) => setForm({ ...form, currentKm: Number(e.target.value) })}
              className="w-full rounded-md border border-slate-300 px-3 py-2"
            />
          </div>

          <div>
            <label className="mb-1 block text-sm font-medium text-slate-700">Horas de motor</label>
            <input
              type="number"
              required
              min={0}
              step="0.1"
              value={form.currentEngineHours}
              onChange={(e) => setForm({ ...form, currentEngineHours: Number(e.target.value) })}
              className="w-full rounded-md border border-slate-300 px-3 py-2"
            />
          </div>

          {formError && <p className="col-span-2 text-sm text-red-600">{formError}</p>}

          <div className="col-span-2 flex gap-2">
            <button
              type="submit"
              disabled={saving}
              className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white hover:bg-slate-800 disabled:opacity-50"
            >
              {saving ? "Salvando..." : "Salvar"}
            </button>
            <button
              type="button"
              onClick={() => setShowForm(false)}
              className="rounded-md border border-slate-300 px-4 py-2 text-sm text-slate-700 hover:bg-white"
            >
              Cancelar
            </button>
          </div>
        </form>
      )}

      {loading && <p className="text-slate-600">Carregando...</p>}
      {error && <p className="text-sm text-red-600">{error}</p>}

      {!loading && motorcycles.length === 0 && (
        <p className="text-slate-600">Nenhuma moto cadastrada ainda.</p>
      )}

      <ul className="flex flex-col gap-3">
        {motorcycles.map((motorcycle) => (
          <li
            key={motorcycle.id}
            className="flex items-center justify-between rounded-lg border border-slate-200 p-4"
          >
            <div>
              <p className="font-medium text-slate-900">
                {motorcycle.brand} {motorcycle.model} ({motorcycle.modelYear})
              </p>
              <p className="text-sm text-slate-600">
                Placa {motorcycle.plate} · {motorcycle.currentKm} km · {motorcycle.currentEngineHours} h
              </p>
            </div>
            <div className="flex gap-2 text-sm">
              <Link
                to={`/motorcycles/${motorcycle.id}/maintenances`}
                className="rounded-md border border-slate-300 px-3 py-1.5 text-slate-700 hover:bg-slate-50"
              >
                Manutencoes
              </Link>
              <button
                onClick={() => openEditForm(motorcycle)}
                className="rounded-md border border-slate-300 px-3 py-1.5 text-slate-700 hover:bg-slate-50"
              >
                Editar
              </button>
              <button
                onClick={() => handleDelete(motorcycle.id)}
                className="rounded-md border border-red-200 px-3 py-1.5 text-red-700 hover:bg-red-50"
              >
                Excluir
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}

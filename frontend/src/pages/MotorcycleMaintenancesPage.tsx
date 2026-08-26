import { useEffect, useState, type FormEvent } from "react";
import { Link, useParams } from "react-router-dom";
import { listMotorcycles } from "../api/motorcycles";
import { createMaintenance, listMaintenanceTypes, listMaintenances, listUpcomingMaintenances } from "../api/maintenances";
import { ApiError } from "../api/client";
import type {
  MaintenanceRecordResponse,
  MaintenanceTypeResponse,
  MotorcycleResponse,
  UpcomingMaintenanceResponse,
} from "../types/dtos";

interface FormState {
  maintenanceTypeId: string;
  serviceDate: string; // yyyy-mm-dd, do <input type="date">
  odometerKm: string;
  engineHours: string;
  cost: string;
  notes: string;
}

const EMPTY_FORM: FormState = {
  maintenanceTypeId: "",
  serviceDate: new Date().toISOString().slice(0, 10),
  odometerKm: "",
  engineHours: "",
  cost: "",
  notes: "",
};

function statusBadgeClass(status: UpcomingMaintenanceResponse["status"]): string {
  return status === "OVERDUE" ? "bg-red-100 text-red-700" : "bg-amber-100 text-amber-700";
}

function statusLabel(status: UpcomingMaintenanceResponse["status"]): string {
  return status === "OVERDUE" ? "Vencido" : "Proximo";
}

export function MotorcycleMaintenancesPage() {
  const { id } = useParams<{ id: string }>();
  const motorcycleId = Number(id);

  const [motorcycle, setMotorcycle] = useState<MotorcycleResponse | null>(null);
  const [upcoming, setUpcoming] = useState<UpcomingMaintenanceResponse[]>([]);
  const [history, setHistory] = useState<MaintenanceRecordResponse[]>([]);
  const [types, setTypes] = useState<MaintenanceTypeResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [formError, setFormError] = useState<string | null>(null);

  async function reload() {
    setLoading(true);
    setError(null);
    try {
      // GET /motorcycles/{id} sozinho nao existe na API (so lista/PUT/DELETE) -
      // reaproveita a listagem completa e filtra pelo id no cliente.
      const [motorcycles, upcomingList, historyList, typesList] = await Promise.all([
        listMotorcycles(),
        listUpcomingMaintenances(motorcycleId),
        listMaintenances(motorcycleId),
        listMaintenanceTypes(),
      ]);
      setMotorcycle(motorcycles.find((m) => m.id === motorcycleId) ?? null);
      setUpcoming(upcomingList);
      setHistory(historyList);
      setTypes(typesList);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Nao foi possivel carregar os dados.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    reload();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [motorcycleId]);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    setFormError(null);
    try {
      await createMaintenance(motorcycleId, {
        maintenanceTypeId: Number(form.maintenanceTypeId),
        // input type="date" so manda a data (yyyy-mm-dd); completa com hora
        // pra virar um ISO datetime que o backend (java.util.Date) aceita.
        serviceDate: `${form.serviceDate}T00:00:00.000Z`,
        odometerKm: form.odometerKm === "" ? null : Number(form.odometerKm),
        engineHours: form.engineHours === "" ? null : Number(form.engineHours),
        cost: form.cost === "" ? null : Number(form.cost),
        notes: form.notes === "" ? null : form.notes,
      });
      setForm(EMPTY_FORM);
      setShowForm(false);
      await reload();
    } catch (err) {
      setFormError(err instanceof ApiError ? err.message : "Nao foi possivel registrar a manutencao.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="mx-auto max-w-3xl px-4 py-8">
      <Link to="/motorcycles" className="mb-4 inline-block text-sm text-slate-600 underline">
        ← Voltar para minhas motos
      </Link>

      <h1 className="mb-1 text-2xl font-semibold text-slate-900">
        {motorcycle ? `${motorcycle.brand} ${motorcycle.model}` : "Manutencoes"}
      </h1>
      {motorcycle && (
        <p className="mb-6 text-sm text-slate-600">
          Placa {motorcycle.plate} · {motorcycle.currentKm} km · {motorcycle.currentEngineHours} h
        </p>
      )}

      {loading && <p className="text-slate-600">Carregando...</p>}
      {error && <p className="text-sm text-red-600">{error}</p>}

      {!loading && (
        <>
          <section className="mb-8">
            <h2 className="mb-2 text-lg font-medium text-slate-900">Proximas manutencoes</h2>
            {upcoming.length === 0 ? (
              <p className="text-sm text-slate-600">Nada proximo ou vencido no momento.</p>
            ) : (
              <ul className="flex flex-col gap-2">
                {upcoming.map((item) => (
                  <li
                    key={item.maintenanceTypeId}
                    className="flex items-center justify-between rounded-lg border border-slate-200 p-3"
                  >
                    <span className="text-sm text-slate-800">{item.maintenanceTypeName}</span>
                    <div className="flex items-center gap-3 text-sm text-slate-600">
                      {item.remainingKm !== null && <span>{item.remainingKm} km restantes</span>}
                      {item.remainingEngineHours !== null && (
                        <span>{item.remainingEngineHours.toFixed(1)} h restantes</span>
                      )}
                      <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${statusBadgeClass(item.status)}`}>
                        {statusLabel(item.status)}
                      </span>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </section>

          <section>
            <div className="mb-2 flex items-center justify-between">
              <h2 className="text-lg font-medium text-slate-900">Historico</h2>
              <button
                onClick={() => setShowForm((v) => !v)}
                className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white hover:bg-slate-800"
              >
                {showForm ? "Cancelar" : "+ Registrar manutencao"}
              </button>
            </div>

            {showForm && (
              <form
                onSubmit={handleSubmit}
                className="mb-6 grid grid-cols-2 gap-4 rounded-lg border border-slate-200 bg-slate-50 p-4"
              >
                <div className="col-span-2">
                  <label className="mb-1 block text-sm font-medium text-slate-700">Tipo de manutencao</label>
                  <select
                    required
                    value={form.maintenanceTypeId}
                    onChange={(e) => setForm({ ...form, maintenanceTypeId: e.target.value })}
                    className="w-full rounded-md border border-slate-300 px-3 py-2"
                  >
                    <option value="" disabled>
                      Selecione...
                    </option>
                    {types.map((type) => (
                      <option key={type.id} value={type.id}>
                        {type.name}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="mb-1 block text-sm font-medium text-slate-700">Data do servico</label>
                  <input
                    type="date"
                    required
                    max={new Date().toISOString().slice(0, 10)}
                    value={form.serviceDate}
                    onChange={(e) => setForm({ ...form, serviceDate: e.target.value })}
                    className="w-full rounded-md border border-slate-300 px-3 py-2"
                  />
                </div>

                <div>
                  <label className="mb-1 block text-sm font-medium text-slate-700">Custo (opcional)</label>
                  <input
                    type="number"
                    min={0}
                    step="0.01"
                    value={form.cost}
                    onChange={(e) => setForm({ ...form, cost: e.target.value })}
                    className="w-full rounded-md border border-slate-300 px-3 py-2"
                  />
                </div>

                <div>
                  <label className="mb-1 block text-sm font-medium text-slate-700">Km no servico (opcional)</label>
                  <input
                    type="number"
                    min={0}
                    value={form.odometerKm}
                    onChange={(e) => setForm({ ...form, odometerKm: e.target.value })}
                    className="w-full rounded-md border border-slate-300 px-3 py-2"
                  />
                </div>

                <div>
                  <label className="mb-1 block text-sm font-medium text-slate-700">Horas de motor (opcional)</label>
                  <input
                    type="number"
                    min={0}
                    step="0.1"
                    value={form.engineHours}
                    onChange={(e) => setForm({ ...form, engineHours: e.target.value })}
                    className="w-full rounded-md border border-slate-300 px-3 py-2"
                  />
                </div>

                <div className="col-span-2">
                  <label className="mb-1 block text-sm font-medium text-slate-700">Notas (opcional)</label>
                  <textarea
                    value={form.notes}
                    onChange={(e) => setForm({ ...form, notes: e.target.value })}
                    className="w-full rounded-md border border-slate-300 px-3 py-2"
                    rows={2}
                  />
                </div>

                {formError && <p className="col-span-2 text-sm text-red-600">{formError}</p>}

                <div className="col-span-2">
                  <button
                    type="submit"
                    disabled={saving}
                    className="rounded-md bg-slate-900 px-4 py-2 text-sm text-white hover:bg-slate-800 disabled:opacity-50"
                  >
                    {saving ? "Salvando..." : "Salvar"}
                  </button>
                </div>
              </form>
            )}

            {history.length === 0 ? (
              <p className="text-sm text-slate-600">Nenhuma manutencao registrada ainda.</p>
            ) : (
              <ul className="flex flex-col gap-2">
                {history.map((record) => (
                  <li key={record.id} className="rounded-lg border border-slate-200 p-3">
                    <div className="flex items-center justify-between">
                      <span className="font-medium text-slate-900">{record.maintenanceTypeName}</span>
                      <span className="text-sm text-slate-600">
                        {new Date(record.serviceDate).toLocaleDateString("pt-BR")}
                      </span>
                    </div>
                    <p className="text-sm text-slate-600">
                      {record.odometerKm !== null && `${record.odometerKm} km`}
                      {record.engineHours !== null && ` · ${record.engineHours} h`}
                      {record.cost !== null && ` · R$ ${record.cost.toFixed(2)}`}
                    </p>
                    {record.notes && <p className="mt-1 text-sm text-slate-500">{record.notes}</p>}
                  </li>
                ))}
              </ul>
            )}
          </section>
        </>
      )}
    </div>
  );
}

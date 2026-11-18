import { useMemo, useState } from 'react'
import { useAuth } from '@/features/auth/context/useAuth'
import type { User } from '@/features/auth/types/auth.types'
import { useAdminUsers } from '../hooks/useAdminUsers'
import './AdminUsersPage.css'
import '../components/UserTable/UserTable.css'

type MemberTab = 'pending' | 'active'

export default function AdminMembersPage() {
  const {
    users,
    isLoading,
    error,
    membershipUpdatingId,
    memberNumberUpdatingId,
    founderUpdatingId,
    feedback,
    retry,
    changeMembership,
    changeMemberNumber,
    changeFounder,
    clearFeedback,
  } = useAdminUsers()
  const { user, refreshUser, logout } = useAuth()
  const [tab, setTab] = useState<MemberTab>('pending')
  const [search, setSearch] = useState('')
  const [draftNumbers, setDraftNumbers] = useState<Record<number, string>>({})

  const updateOwnSession = async (target: User) => {
    if (target.id !== user?.id) return
    try {
      await refreshUser()
    } catch {
      logout()
    }
  }

  const updateMembership = async (target: User, active: boolean) => {
    clearFeedback()

    const prompt = active
      ? `¿Has comprobado que ${target.username} pertenece a la comunidad de GuatequeRiders? Revisa el grupo general y confirma su pertenencia antes de activar el carnet.`
      : `¿Has confirmado que ${target.username} ya no pertenece a la comunidad de GuatequeRiders? Salir solo del grupo general no significa necesariamente salir de toda la comunidad. ¿Desactivar su carnet?`

    if (!window.confirm(prompt)) return

    try {
      await changeMembership(target.id, active)
      await updateOwnSession(target)
    } catch {
      return
    }
  }

  const saveMemberNumber = async (target: User) => {
    const requested = (draftNumbers[target.id] ?? target.memberNumber ?? '').trim()
    if (!requested) return
    clearFeedback()

    try {
      await changeMemberNumber(target.id, requested)
      setDraftNumbers((current) => {
        const next = { ...current }
        delete next[target.id]
        return next
      })
      await updateOwnSession(target)
    } catch {
      return
    }
  }

  const updateFounder = async (target: User) => {
    clearFeedback()
    const action = target.founder ? 'quitar la condición de fundador' : 'marcar como fundador'
    if (!window.confirm(`¿Quieres ${action} a ${target.username}?`)) return

    try {
      const updated = await changeFounder(target.id, !target.founder)
      if (!updated.founder) {
        setDraftNumbers((current) => {
          const next = { ...current }
          delete next[target.id]
          return next
        })
      }
      await updateOwnSession(target)
    } catch {
      return
    }
  }

  const people = useMemo(() => users.filter((account) => account.role !== 'COLLABORATOR'), [users])
  const pending = useMemo(
    () => people.filter((account) => !account.memberActive).sort((a, b) => a.id - b.id),
    [people],
  )
  const active = useMemo(
    () => people.filter((account) => account.memberActive).sort((a, b) => a.username.localeCompare(b.username, 'es')),
    [people],
  )
  const visibleMembers = useMemo(() => {
    const query = search.trim().toLocaleLowerCase('es')
    const selection = tab === 'pending' ? pending : active
    if (!query) return selection
    return selection.filter((account) =>
      [account.username, account.email, account.phone ?? '', account.memberNumber ?? '']
        .some((value) => value.toLocaleLowerCase('es').includes(query)),
    )
  }, [tab, search, pending, active])

  return (
    <section className="admin-users admin-members">
      <header className="admin-users__header">
        <h1>Gestión de socios</h1>
        <p>
          Para obtener el carnet hay que pertenecer a la comunidad de WhatsApp de GuatequeRiders.
          Podéis comprobar las altas en el grupo general y confirmar la pertenencia antes de aprobarlas.
          La web no puede leer por sí sola los integrantes de la comunidad.
        </p>
      </header>

      <div className="admin-members__guidance">
        <strong>Referencia para comprobar: grupo general de WhatsApp</strong>
        <p>
          Es un grupo dentro de la comunidad. Una persona que no aparezca en el grupo general
          puede seguir perteneciendo a otro grupo de la comunidad. No retires el carnet
          sin confirmar que ha abandonado la comunidad completa.
        </p>
      </div>

      {feedback && (
        <div className={`admin-users__feedback admin-users__feedback--${feedback.type}`} role="status">
          <span>{feedback.message}</span>
          <button
            className="admin-users__feedback-close"
            onClick={clearFeedback}
            type="button"
            aria-label="Cerrar"
          >
            ✕
          </button>
        </div>
      )}

      {error && (
        <div className="admin-users__error" role="alert">
          <span>{error}</span>
          <button className="btn btn--outline-danger btn--sm" onClick={retry} type="button">
            Reintentar
          </button>
        </div>
      )}

      <div className="admin-members__toolbar">
        <div className="admin-members__tabs" role="tablist" aria-label="Estado de los socios">
          <button
            id="members-tab-pending"
            type="button"
            role="tab"
            aria-selected={tab === 'pending'}
            aria-controls="members-tab-panel"
            className={`admin-members__tab${tab === 'pending' ? ' admin-members__tab--selected' : ''}`}
            onClick={() => { setTab('pending'); setSearch(''); clearFeedback() }}
          >
            Pendientes de verificar <span>{pending.length}</span>
          </button>
          <button
            id="members-tab-active"
            type="button"
            role="tab"
            aria-selected={tab === 'active'}
            aria-controls="members-tab-panel"
            className={`admin-members__tab${tab === 'active' ? ' admin-members__tab--selected' : ''}`}
            onClick={() => { setTab('active'); setSearch(''); clearFeedback() }}
          >
            Socios activos <span>{active.length}</span>
          </button>
        </div>
        <div className="admin-members__filters">
          <label className="admin-members__search">
            <span className="admin-members__visually-hidden">Buscar personas</span>
            <input
              type="search"
              value={search}
              placeholder="Buscar nombre, email, teléfono o número..."
              onChange={(event) => setSearch(event.target.value)}
            />
          </label>
          <button className="btn btn--ghost btn--sm" type="button" onClick={() => void retry()} disabled={isLoading}>
            Actualizar listado
          </button>
        </div>
      </div>

      <div id="members-tab-panel" role="tabpanel" aria-labelledby={tab === 'pending' ? 'members-tab-pending' : 'members-tab-active'}>
        <div className="admin-members__section-heading">
          <div>
            <h2>{tab === 'pending' ? 'Pendientes de comprobar' : 'Carnets activos'}</h2>
            <p>
              {tab === 'pending'
                ? 'Estas cuentas todavía no tienen acceso al carnet ni a descuentos. Confirma su pertenencia a la comunidad, usando el grupo general como referencia.'
                : 'Estos usuarios tienen carnet y descuentos habilitados. Desactiva únicamente a quienes hayan abandonado la comunidad.'}
            </p>
          </div>
          <span className="admin-members__results">{visibleMembers.length} resultados</span>
        </div>

        {isLoading ? (
          <div className="admin-users__loading" role="status" aria-live="polite">
            <p>Cargando usuarios...</p>
          </div>
        ) : visibleMembers.length === 0 ? (
          <div className="admin-users__empty">
            <p>
              {search.trim()
                ? 'No se han encontrado personas con esa búsqueda.'
                : tab === 'pending'
                  ? 'No hay cuentas pendientes de comprobar.'
                  : 'Todavía no hay socios activos.'}
            </p>
          </div>
        ) : (
          <div className="user-table__wrapper">
            <table className="user-table">
              <thead>
                <tr>
                  <th scope="col">Usuario</th>
                  <th scope="col">N.º socio</th>
                  <th scope="col">Fundador</th>
                  <th scope="col">Email</th>
                  <th scope="col">Teléfono</th>
                  <th scope="col">Estado</th>
                  <th scope="col">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {visibleMembers.map((member) => {
                  const updating = membershipUpdatingId === member.id
                  const updatingNumber = memberNumberUpdatingId === member.id
                  const updatingFounder = founderUpdatingId === member.id
                  const busy = updating || updatingNumber || updatingFounder
                  const numberValue = draftNumbers[member.id] ?? member.memberNumber ?? ''
                  const hasNumberChange = numberValue.trim().toUpperCase() !== (member.memberNumber ?? '')
                  const isAdmin = member.role === 'ADMIN'

                  return (
                    <tr key={member.id}>
                      <td className="user-table__username" data-label="Usuario">{member.username}</td>
                      <td data-label="N.º socio">
                        {member.founder ? (
                          <div className="admin-members__number-editor">
                            <input
                              type="text"
                              aria-label={`Número de socio de ${member.username}`}
                              value={numberValue}
                              maxLength={32}
                              placeholder="GR-0001"
                              autoCapitalize="characters"
                              spellCheck={false}
                              disabled={busy}
                              onChange={(event) =>
                                setDraftNumbers((current) => ({
                                  ...current,
                                  [member.id]: event.target.value.toUpperCase(),
                                }))
                              }
                            />
                            <button
                              className="btn btn--ghost btn--sm"
                              type="button"
                              disabled={!hasNumberChange || !numberValue.trim() || busy}
                              onClick={() => void saveMemberNumber(member)}
                            >
                              {updatingNumber ? 'Guardando...' : 'Guardar'}
                            </button>
                          </div>
                        ) : (
                          <span className="admin-members__automatic-number">
                            {member.memberNumber ?? 'Se asignará al activar'}
                          </span>
                        )}
                      </td>
                      <td data-label="Fundador">
                        <button
                          className="btn btn--ghost btn--sm"
                          type="button"
                          disabled={busy}
                          onClick={() => void updateFounder(member)}
                        >
                          {updatingFounder ? 'Actualizando...' : member.founder ? 'Quitar fundador' : 'Marcar fundador'}
                        </button>
                      </td>
                      <td data-label="Email">{member.email}</td>
                      <td data-label="Teléfono">{member.phone ?? 'No informado'}</td>
                      <td data-label="Estado">
                        <span className={`admin-members__status${member.memberActive ? ' admin-members__status--active' : ''}`}>
                          {member.memberActive ? 'Socio activo' : 'Pendiente'}
                        </span>
                        {isAdmin && <span className="admin-members__admin-label"> · Admin</span>}
                      </td>
                      <td className="user-table__actions" data-label="Acciones">
                        {member.role === 'USER' ? (
                          <button
                            className={`btn btn--sm ${member.memberActive ? 'btn--outline-danger' : 'btn--primary'}`}
                            type="button"
                            disabled={busy}
                            onClick={() => void updateMembership(member, !member.memberActive)}
                          >
                            {updating ? 'Guardando...' : member.memberActive ? 'Quitar socio' : 'Confirmar y activar'}
                          </button>
                        ) : (
                          <span className="admin-members__locked">Carnet de administrador</span>
                        )}
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  )
}

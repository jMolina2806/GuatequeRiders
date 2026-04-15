import type { Participant } from '../../types/route.types'

type ParticipantListProps = {
  participants: Participant[]
}

export default function ParticipantList({
  participants,
}: ParticipantListProps) {
  return (
    <div>
      <h4>Participantes</h4>
      <ul>
        {participants.map((participant) => (
          <li key={participant.id}>{participant.name}</li>
        ))}
      </ul>
    </div>
  )
}
export type RoomStatus = "WAITING" | "PREPARING" | "RUNNING" | "FINISHED" | "ARCHIVED";
export type PlayerStatus = "ALIVE" | "ELIMINATED";
export type ShotOutcome =
  | "MISS"
  | "OBJECT_HIT"
  | "OBJECT_DESTROYED"
  | "AVATAR_HIT"
  | "AVATAR_ELIMINATED";

export interface Coord {
  x: number;
  y: number;
}

export interface PlayerView {
  id: string;
  nickname: string;
  color: string;
  lives: number;
  score: number;
  status: PlayerStatus;
  ready: boolean;
}

export interface ObjectView {
  id: string;
  type: string;
  x: number;
  y: number;
  health: number;
  maxHealth: number;
  destroyed: boolean;
}

export interface ShotView {
  x: number;
  y: number;
  byPlayerId: string;
  outcome: ShotOutcome;
  objectType: string | null;
}

export interface OwnOffice {
  width: number;
  height: number;
  avatar: Coord | null;
  objects: ObjectView[];
  shots: ShotView[];
}

export interface RevealedCell {
  x: number;
  y: number;
  outcome: ShotOutcome;
  objectType: string | null;
}

export interface OpponentOffice {
  width: number;
  height: number;
  revealed: RevealedCell[];
}

export interface OpponentView {
  id: string;
  nickname: string;
  color: string;
  lives: number;
  score: number;
  status: PlayerStatus;
  ready: boolean;
  office: OpponentOffice;
}

export interface FeedView {
  seq: number;
  type: string;
  message: string;
  timestamp: number;
}

export interface RoomState {
  code: string;
  name: string;
  modeId: string;
  status: RoomStatus;
  currentPlayerId: string | null;
  winnerId: string | null;
  you: string | null;
  officeWidth: number;
  officeHeight: number;
  players: PlayerView[];
  yourOffice: OwnOffice | null;
  opponents: OpponentView[];
  feed: FeedView[];
}

export interface CreatedRoom {
  code: string;
}

export interface RoomSummary {
  code: string;
  name: string;
  modeId: string;
  status: RoomStatus;
  players: number;
  maxPlayers: number;
}

export interface Joined {
  playerId: string;
  nickname: string;
  color: string;
}

export interface Placement {
  type: string;
  x: number;
  y: number;
}

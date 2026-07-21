import { useEffect, useRef } from "react";
import type { FeedView } from "../api/types";

const ICON: Record<string, string> = {
  SYSTEM: "⚙️",
  JOIN: "➕",
  READY: "✅",
  MISS: "💧",
  OBJECT_HIT: "💢",
  OBJECT_DESTROYED: "💥",
  AVATAR_HIT: "🎯",
  AVATAR_ELIMINATED: "☠️",
  WIN: "🏆",
};

export function Feed({ feed }: { feed: FeedView[] }) {
  const endRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [feed.length]);

  return (
    <div className="feed card">
      <div className="panel-title">Historial</div>
      <div className="feed-list">
        {feed.map((f) => (
          <div key={f.seq} className={`feed-item type-${f.type.toLowerCase()}`}>
            <span>{ICON[f.type] ?? "•"}</span>
            <span>{f.message}</span>
          </div>
        ))}
        <div ref={endRef} />
      </div>
    </div>
  );
}

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
  const listRef = useRef<HTMLDivElement>(null);
  const newestFirst = [...feed].reverse();

  useEffect(() => {
    listRef.current?.scrollTo({ top: 0, behavior: "smooth" });
  }, [feed.length]);

  return (
    <div className="feed card" id="feed-panel">
      <div className="panel-title">Historial</div>
      <div className="feed-list" ref={listRef}>
        {newestFirst.map((f) => (
          <div key={f.seq} className={`feed-item type-${f.type.toLowerCase()}`}>
            <span>{ICON[f.type] ?? "•"}</span>
            <span>{f.message}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

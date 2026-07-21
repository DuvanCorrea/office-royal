import { useSession } from "../state/store";
import { JoinScreen } from "../ui/JoinScreen";
import { GameScreen } from "../ui/GameScreen";

export function App() {
  const session = useSession((s) => s.session);
  return <div className="app">{session ? <GameScreen /> : <JoinScreen />}</div>;
}

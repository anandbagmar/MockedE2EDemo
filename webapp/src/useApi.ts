import { DependencyList, useEffect, useState } from 'react';

export type ApiState<T> =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'ready'; data: T };

// Loads data when a screen mounts. Responses that arrive after unmount (or after StrictMode's
// second effect run) are ignored, so the screen always renders the latest request only.
export function useApi<T>(load: () => Promise<T>, deps: DependencyList): ApiState<T> {
  const [state, setState] = useState<ApiState<T>>({ status: 'loading' });

  useEffect(() => {
    let active = true;
    setState({ status: 'loading' });
    load().then(
      (data) => active && setState({ status: 'ready', data }),
      (e: Error) => active && setState({ status: 'error', message: e.message }),
    );
    return () => {
      active = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  return state;
}

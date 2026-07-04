import { useEffect, useRef, useCallback } from 'react';
import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { getToken } from '../api/tokenManager';

const SOCKET_URL = import.meta.env.VITE_WS_URL || (import.meta.env.DEV ? 'http://localhost:8080/ws' : null);

if (!SOCKET_URL) {
  throw new Error('VITE_WS_URL environment variable is required');
}

interface UseWebSocketOptions {
  onConnect?: () => void;
  onDisconnect?: () => void;
}

function createClient(): Client {
  const token = getToken();
  return new Client({
    webSocketFactory: () => new SockJS(SOCKET_URL),
    connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
  });
}

export function useWebSocket(options?: UseWebSocketOptions) {
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    const client = createClient();

    client.onConnect = () => {
      options?.onConnect?.();
    };

    client.onDisconnect = () => {
      options?.onDisconnect?.();
    };

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  const subscribe = useCallback((topic: string, callback: (body: string) => void) => {
    const client = clientRef.current;
    if (!client?.connected) return null;

    const subscription = client.subscribe(topic, (message: IMessage) => {
      callback(message.body);
    });

    return () => {
      subscription.unsubscribe();
    };
  }, []);

  return { subscribe, client: clientRef };
}

export function usePartidaWebSocket(
  partidaId: number | undefined,
  onUpdate: (data: unknown) => void,
  onEvent?: (data: unknown) => void,
) {
  const updateRef = useRef(onUpdate);
  const eventRef = useRef(onEvent);

  useEffect(() => {
    updateRef.current = onUpdate;
    eventRef.current = onEvent;
  });

  useEffect(() => {
    if (!partidaId) return;

    const client = createClient();

    client.onConnect = () => {
      client.subscribe(`/topic/partidas/${partidaId}`, (message: IMessage) => {
        try {
          const data = JSON.parse(message.body);
          updateRef.current(data);
        } catch (err) {
          console.warn('WebSocket: erro ao parsear atualização da partida', err);
        }
      });

      client.subscribe(`/topic/partidas/${partidaId}/eventos`, (message: IMessage) => {
        try {
          const data = JSON.parse(message.body);
          eventRef.current?.(data);
        } catch (err) {
          console.warn('WebSocket: erro ao parsear evento da partida', err);
        }
      });
    };

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [partidaId]);
}

package events

typealias EventHandler = (AppEvent) -> Unit

// Bus de eventos
object EventBus {
    private val handlers = mutableListOf<EventHandler>()

    fun subscribe(handler: EventHandler) { handlers += handler }
    fun unsubscribe(handler: EventHandler) { handlers -= handler }

    // Publica evento a los handlers
    fun publish(event: AppEvent) {
        handlers.forEach { h ->
            try { h(event) } catch (_: Exception) { /* no romper flujo */ }
        }
    }
}

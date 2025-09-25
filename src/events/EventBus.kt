package events

typealias EventHandler = (AppEvent) -> Unit

object EventBus {
    private val handlers = mutableListOf<EventHandler>()

    fun subscribe(handler: EventHandler) { handlers += handler }
    fun unsubscribe(handler: EventHandler) { handlers -= handler }

    fun publish(event: AppEvent) {
        handlers.forEach { h ->
            try { h(event) } catch (_: Exception) { /* no romper flujo */ }
        }
    }
}

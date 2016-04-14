
/**
 JS reducer that generates an object containing the active users.
 **/
function(prev, next) {
    if (next['event-type'] == "UserRegistered") {
        prev[next.payload.username]=true;
    } else {
        delete prev[next.payload.username]
    }
    return prev;
}
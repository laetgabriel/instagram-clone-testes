const API_URL = "http://localhost:8080/api/v1";

export async function loginUser(credentials) {
    const res = await fetch(`${API_URL}/auth/signin`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(credentials),
    });

    const data = await res.json();

    if (!res.ok) {
        throw new Error('Error logging in');
    }

    return data;
}

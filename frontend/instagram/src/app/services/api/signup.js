const API_URL = "http://localhost:8080/api/v1";

export async function createUser(user) {
    const res = await fetch(`${API_URL}/auth/signup`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(user),
    });


    if (!res.ok) {
        const errorData = await res.json();

        if (res.status === 409) {
            throw new Error(errorData.message || 'E-mail or username already in use.');
        }

        throw new Error(errorData.message || 'An error occurred while creating the user.');
    }

    const data = await res.json();
    console.log(data);
    return data;
}

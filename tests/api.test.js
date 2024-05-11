test("expect users", async () => {
    const response = await fetch("http://localhost:3000/api/users");
    const json = await response.json();
    const users = json.users;
    expect(users).toBeTruthy();
    expect(users).toHaveLength(4);
})
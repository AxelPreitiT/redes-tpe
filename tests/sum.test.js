const sum = (a, b) => {
    return a + b;
}

test("sum", () => {
    expect(sum(1, 2)).toBe(4);
});

test("fail", () => {
    expect(sum(1, 2)).not.toBe(4);
});

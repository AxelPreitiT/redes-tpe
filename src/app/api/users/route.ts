export const GET = (request: Request) => {
    return Response.,json({
        users: [{
            id: 0,
            name: "Gaston",
            surname: "Francois"
        },
        {
            id: 1,
            name: "Jose",
            surname: "Mentasti"
        },
        {
            id: 2,
            name: "Tomas",
            surname: "Gaybare"
        },
        {
            id: 3,
            name: "Axel",
            surname: "Preiti"
        }
    ]
    })
}
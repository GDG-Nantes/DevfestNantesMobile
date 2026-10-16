package com.gdgnantes.devfest.core.data.graphql

/**
 * Recorded, trimmed responses of the public confetti-app.dev GraphQL API for the six operations
 * of `:core:network` (D-13). They are Kotlin string constants and not test resources because iOS
 * `commonTest` has no portable resource loading.
 *
 * Recorded on 2026-10-16 for the conference `devfestnantes2025`. Shape of the call, using the
 * generated `OPERATION_DOCUMENT` of each query (it contains the compiler-added `__typename`):
 *
 *     curl -sS -X POST https://confetti-app.dev/graphql \
 *       -H 'Content-Type: application/json' -H 'conference: devfestnantes2025' \
 *       -d '{"query":"<OPERATION_DOCUMENT>","operationName":"GetSessions","variables":{}}'
 *
 * GetVenue used the variables `{"id":"main","language":"en"|"fr"}`, GetSession used
 * `{"id":"thefutureofroutingwiththenavigationapi"}`.
 *
 * Trimming (the JSON is otherwise verbatim, only re-indented): [SESSIONS] keeps 5 of 94 sessions
 * (a keynote, a talk whose `feedbackId` differs from its id, a four-speaker codelab, a codelab
 * with a speaker whose `bluesky` social has an empty url, and the room-less, speaker-less lunch);
 * [SPEAKERS] keeps the speakers of those sessions; [PARTNER_GROUPS] keeps the first partner of each
 * of the five groups. Only public conference data is recorded (D-13, T-5-07).
 */
object RecordedResponses {
    val ROOMS: String = """
{
  "data": {
    "rooms": [
      {
        "__typename": "Room",
        "id": "Belem",
        "name": "Belem"
      },
      {
        "__typename": "Room",
        "id": "Hangar",
        "name": "Hangar"
      },
      {
        "__typename": "Room",
        "id": "Jules Verne",
        "name": "Jules Verne"
      },
      {
        "__typename": "Room",
        "id": "L'Atelier",
        "name": "L'Atelier"
      },
      {
        "__typename": "Room",
        "id": "Les Machines",
        "name": "Les Machines"
      },
      {
        "__typename": "Room",
        "id": "Titan",
        "name": "Titan"
      },
      {
        "__typename": "Room",
        "id": "Tour Bretagne",
        "name": "Tour Bretagne"
      },
      {
        "__typename": "Room",
        "id": "Unconf",
        "name": "Unconf"
      }
    ]
  }
}
"""

    val SESSIONS: String = """
{
  "data": {
    "sessions": {
      "__typename": "SessionConnection",
      "nodes": [
        {
          "__typename": "Session",
          "id": "keynotedouverture",
          "title": "Opening Keynote - Advice Well Taken",
          "complexity": "Beginner",
          "description": "En 2023, Ilina a passé une année à documenter ce qu'elle appelle le techlore, un « folklore » sur les fonctionnalités complexes et opaques des technologies modernes qui nous entourent. L'artiste aborde les questions relatives à nos données et à notre vie privée, les raccourcis que nous utilisons pour réparer nos appareils et les explications que nous trouvons aux mystères technologiques. \n\nPour ce projet, Ilina a recueilli des contes populaires sur le salut numérique : des histoires quotidiennes d'anthropomorphisation, de piratage et de contournement. Elles montrent comment les gens ordinaires font ce qu'ils peuvent pour affirmer leur contrôle sur la technologie. Dasha Ilina a développé ADVICE WELL TAKEN lors de sa résidence EMAP à IMPAKT en mai et juin 2023, en collaboration avec Supisara Burapachaisri.",
          "language": "fr-FR",
          "startsAt": "2025-10-16T09:00",
          "endsAt": "2025-10-16T09:50",
          "rooms": [
            {
              "__typename": "Room",
              "id": "Jules Verne",
              "name": "Jules Verne"
            }
          ],
          "speakers": [
            {
              "__typename": "Speaker",
              "id": "dasha_ilina",
              "bio": "Dasha Ilina is a Russian techno-critical artist based in Paris, France. Through the employment of low-tech and DIY approaches, her work questions the desire to incorporate modern technology into our daily lives by highlighting the implications of actually doing so.\n\nHer practice engages the public in order to facilitate a space for the development of critical thought regarding social imperatives for care of oneself and others, privacy in the digital age, and the reflexive contemporary urge to turn to technology for answers.\n\nShe is the founder of the Center for Technological Pain, a project that proposes DIY solutions to health problems caused by digital technologies for which she has received an Honorary Mention at Ars Electronica. Her project Technosommeil is in the collection of digital art works of the Département Val-de-Marne (Mallapixels). Ilina’s work has been exhibited at institutions such as Centre Pompidou (FR), MU Artspace (NL), Gaîté Lyrique (FR), Hartware Medienkunstverein Dortmund (DE), NeMe (CY), ISEA 2023 (FR) as well as various talks, workshops, and performances held internationally.\n\nShe is also the co-director of NØ SCHOOL, a summer school that focuses on critical research around the social and environmental impacts of information and communication technologies.",
              "company": "Artist",
              "companyLogoUrl": null,
              "city": "Paris, France",
              "name": "Dasha Ilina",
              "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/dasha_ilina.jpg",
              "socials": [
                {
                  "__typename": "Social",
                  "name": "linkedin",
                  "url": "https://www.linkedin.com/dasha-ilina-633005132",
                  "icon": null
                }
              ]
            }
          ],
          "feedbackId": "keynotedouverture",
          "tags": [
            "💡 Discovery"
          ],
          "type": "conference",
          "links": []
        },
        {
          "__typename": "Session",
          "id": "thefutureofroutingwiththenavigationapi",
          "title": "The future of routing with the Navigation API",
          "complexity": "Intermediate",
          "description": "Today, virtually all Single Page Applications (SPAs) use a router. Each frontend framework offers one (or more), with often very similar APIs. Frontend routing is generally considered a solved problem. However, the reality is quite different: frontend routing relies on browser APIs that are frankly outdated.\n\nIn this presentation, we will explore the APIs that have shaped routing as we know it today. Then, we will discover what the future holds with the **Navigation API**, a promising new interface that could very well be a game-changer.",
          "language": "en-US",
          "startsAt": "2025-10-16T14:00",
          "endsAt": "2025-10-16T14:50",
          "rooms": [
            {
              "__typename": "Room",
              "id": "Tour Bretagne",
              "name": "Tour Bretagne"
            }
          ],
          "speakers": [
            {
              "__typename": "Speaker",
              "id": "eduardo_san_martin_morote",
              "bio": "Eduardo est développeur frontend freelance, membre à temps partiel de l’équipe Core de Vue.js, et auteur de libs telles que Vue Router, Pinia, VueFire, entre autres. Il est très impliqué dans l’open source et passionné par la création d’architectures frontend maintenables et testées.",
              "company": "Freelance",
              "companyLogoUrl": null,
              "city": "Paris, France",
              "name": "Eduardo San Martin Morote",
              "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/eduardo_san_martin_morote.jpg",
              "socials": [
                {
                  "__typename": "Social",
                  "name": "twitter",
                  "url": "https://twitter.com/posva",
                  "icon": null
                },
                {
                  "__typename": "Social",
                  "name": "linkedin",
                  "url": "https://www.linkedin.com/posva",
                  "icon": null
                }
              ]
            }
          ],
          "feedbackId": "lefuturduroutingaveclanavigationapi",
          "tags": [
            "🌍 Web"
          ],
          "type": "conference",
          "links": []
        },
        {
          "__typename": "Session",
          "id": "laguerredespromptsattaquesdefensesauroyaumedesllm",
          "title": "La Guerre des Prompts : attaques & défenses au royaume des LLM ⚔️🛡️🤖",
          "complexity": "Intermediate",
          "description": "> _Un terrible hiver se prépare au royaume des chatbots. Une armée de ténèbres se lève pour détourner les grands modèles de langages (LLM). Quelques mots bien choisis peuvent suffire à faire tomber les défenses, et tous les coups sont permis._\n\nDans cette session, vous découvrirez les attaques les plus fréquentes affectant les chatbots gonflés à l'IAGen. Grâce à l’OWASP Top 10 for LLM, le Secure AI Framework (SAIF), des interventions d’experts au BlackHat, vous comprendrez les risques et menaces qui visent le Prompt.\n\nVous pratiquerez des exercices de _jailbreak_ inspirés de publications universitaires et du pentest, s’appuyant sur des vulnérabilités rencontrées en conditions réelles. Avec des outils open source comme Garak, Adversarial Robustness Toolbox, PyRIT (et d'autres) vous testerez la robustesse des LLM et des contre-mesures possibles.\n\nÀ l’issue de l'atelier, vous saurez non seulement expliquer les principes du Prompt Injection, mais aussi mettre en oeuvre des protections adaptées pour renforcer la sécurité de vos applications.\n\n> _Rois, reines, chevaliers et renégats se disputent le royaume des LLM.\n> Lesquels arriveront à leurs fins ? Les plus forts ? Ou les plus retors ?_\n\n🧑‍💻 _NB : vous pourrez installer les outils en amont du codelab, instructions à venir : https://doc.tock.ai/codelab.html_",
          "language": "fr-FR",
          "startsAt": "2025-10-17T09:00",
          "endsAt": "2025-10-17T11:00",
          "rooms": [
            {
              "__typename": "Room",
              "id": "Hangar",
              "name": "Hangar"
            }
          ],
          "speakers": [
            {
              "__typename": "Speaker",
              "id": "benjamin_bernard",
              "bio": "Développeur fullstack j'accorde beaucoup d'importance aux développements sous licences libres et plus globalement en Open Source, baignant de ce milieu depuis de nombreuses années au niveau associatif et professionnel.",
              "company": "Crédit Mutuel Arkéa",
              "companyLogoUrl": null,
              "city": "Brest, France",
              "name": "Benjamin Bernard",
              "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/benjamin_bernard.jpg",
              "socials": [
                {
                  "__typename": "Social",
                  "name": "linkedin",
                  "url": "https://www.linkedin.com/benvii",
                  "icon": null
                }
              ]
            },
            {
              "__typename": "Speaker",
              "id": "francois_nollen",
              "bio": "Staff Engineer chez SNCF Connect & Tech.\nMembre association « TOSIT : The Open Source I Trust » (https://tosit.fr).\nCommitter « Tock: The Open Conversation Kit » (https://doc.tock.ai). \n\nTopics: Flutter ChatBots OpenSource DevOps Observability Git Maven KotlinJVM...",
              "company": "SNCF Connect & Tech",
              "companyLogoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/srcsncf_connect___tech.png",
              "city": "Paris, France",
              "name": "François Nollen",
              "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/francois_nollen.jpg",
              "socials": [
                {
                  "__typename": "Social",
                  "name": "github",
                  "url": "https://github.com/francoisno",
                  "icon": null
                },
                {
                  "__typename": "Social",
                  "name": "twitter",
                  "url": "https://twitter.com/FrancoisN0",
                  "icon": null
                },
                {
                  "__typename": "Social",
                  "name": "linkedin",
                  "url": "https://www.linkedin.com/francois-nollen",
                  "icon": null
                }
              ]
            },
            {
              "__typename": "Speaker",
              "id": "pierre_therrode",
              "bio": "**Lead Developer** chez **SNCF Connect & Tech** sur la partie chatbot depuis près de 5 ans.\nGeek et curieux, j'aime comprendre le fonctionnement des choses qui nous entourent.\nContributeur à la plateforme open source [Tock - The Open Conversation Kit](https://doc.tock.ai/)",
              "company": "SNCF Connect & Tech",
              "companyLogoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/srcsncf_connect___tech.png",
              "city": "Paris, France",
              "name": "Pierre Therrode",
              "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/pierre_therrode.jpg",
              "socials": [
                {
                  "__typename": "Social",
                  "name": "github",
                  "url": "https://github.com/pi-2r",
                  "icon": null
                },
                {
                  "__typename": "Social",
                  "name": "twitter",
                  "url": "https://twitter.com/pi2r3",
                  "icon": null
                },
                {
                  "__typename": "Social",
                  "name": "linkedin",
                  "url": "https://www.linkedin.com/ptherrode",
                  "icon": null
                }
              ]
            },
            {
              "__typename": "Speaker",
              "id": "xavier_charef",
              "bio": "Data Scientist de puis 6 ans à **SNCF Connect & Tech**, j'ai travaillé sur des produit IA pour différentes directions (Client, Marketing et Produit). Depuis 2 ans,  j'ai contribué à migrer notre ancien bot d'aide en ligne **ouvert au grand public** vers une solution à base de **IA générative**. \nPar ailleurs, je suis également formateur interne à SNCF Connect où j'anime différentes formations pour accompagner nos collaborateurs à la démarche IA de l'entreprise et les aider à repérer des Use Case IA au quotidien.",
              "company": "SNCF Connect & Tech",
              "companyLogoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/srcsncf_connect.png",
              "city": "Le Havre, France",
              "name": "Xavier Charef",
              "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/xavier_charef.jpeg",
              "socials": [
                {
                  "__typename": "Social",
                  "name": "linkedin",
                  "url": "https://www.linkedin.com/xavier-charef-6b843497",
                  "icon": null
                }
              ]
            }
          ],
          "feedbackId": "laguerredespromptsattaquesdefensesauroyaumedesllm",
          "tags": [
            "🤖 BigData & AI"
          ],
          "type": "codelab",
          "links": []
        },
        {
          "__typename": "Session",
          "id": "lalegendedeslecteursdecranuneodysseesensorielleaucurdenosapps",
          "title": "La Légende des Lecteurs d'Écran : une odyssée sensorielle au cœur de nos Apps",
          "complexity": "Beginner",
          "description": "Entrez dans un monde où la vue n'est plus le seul chemin vers la connaissance numérique. À l'image des héros de légende bravant les obstacles les yeux bandés, venez défier le royaume de vos smartphones guidés uniquement par la voix douce et mystérieuse des lecteurs d'écran. \nVoiceOver, TalkBack, sémantique... ces incantations modernes vous semblent obscures ? N'ayez crainte, même les novices sont les bienvenus dans cette quête initiatique. \n\nMunissez-vous de votre fidèle destrier (votre smartphone IOS ou Android), de vos écouteurs protecteurs et d'un bandeau d'aventurier (masque ou écharpe). \nEnsemble, nous déchiffrerons les gestes invisibles qui permettent aux utilisateurs non-voyants de naviguer dans ce monde enchanté. \n\nAucune potion de développeur n'est requise pour cette aventure immersive. Alors, laissez-vous guider par la légende et venez explorer l'accessibilité les yeux fermés !",
          "language": "fr-FR",
          "startsAt": "2025-10-16T10:00",
          "endsAt": "2025-10-16T12:00",
          "rooms": [
            {
              "__typename": "Room",
              "id": "Les Machines",
              "name": "Les Machines"
            }
          ],
          "speakers": [
            {
              "__typename": "Speaker",
              "id": "oceane_gillard",
              "bio": "Dev web fullstack et mobile, j'ai de multiples casquettes et aime garder une vision globale sur les projets.\nAyant découvert Flutter en 2020, j'ai tout de suite adoré ! J'ai d'ailleurs développé une application Flutter permettant de compter les points durant une partie de Barbu (un jeu de cartes, aussi connu sous le nom de Tafferan). L'application Score Barbu est disponible sur le PlayStore depuis 2023.\n\nJ'ai rejoint Zenika en 2023, ce qui m'a permis de participer à de nombreux projets et donner mes premières conférences.",
              "company": "Zenika",
              "companyLogoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/srczenika.png",
              "city": "Nantes, France",
              "name": "Océane Gillard",
              "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/oceane_gillard.jpg",
              "socials": [
                {
                  "__typename": "Social",
                  "name": "linkedin",
                  "url": "https://www.linkedin.com/oceane-gillard",
                  "icon": null
                },
                {
                  "__typename": "Social",
                  "name": "github",
                  "url": "https://github.com/Zigotote",
                  "icon": null
                }
              ]
            },
            {
              "__typename": "Speaker",
              "id": "patrice_de_saint_steban",
              "bio": "Depuis plus de 15 ans, je me passionne pour le développement d'applications WEB et Mobile, en touchant à toutes les couches du Front au Back jusqu'à son déploiement sur le Cloud.\n\nJ'ai travaillé sur des technologies très différentes, du PHP au JAVA et JAVASCRIPT, du GWT à Angular et React, de Google App Engine à Node.js, mais également le mobile avec Flutter et l'accessibilité.\nJe suis organisateur du Meetup Flutter Nantes, créateur d'un plugin IntelliJ et de site WordPress pour des associations.",
              "company": "Zenika",
              "companyLogoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/srczenika.png",
              "city": "Nantes, France",
              "name": "Patrice De Saint Steban",
              "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/patrice_de_saint_steban.jpg",
              "socials": [
                {
                  "__typename": "Social",
                  "name": "github",
                  "url": "https://github.com/patou",
                  "icon": null
                },
                {
                  "__typename": "Social",
                  "name": "bluesky",
                  "url": "",
                  "icon": null
                },
                {
                  "__typename": "Social",
                  "name": "linkedin",
                  "url": "https://www.linkedin.com/patrice-de-saint-steban",
                  "icon": null
                }
              ]
            }
          ],
          "feedbackId": "lalegendedeslecteursdecranuneodysseesensorielleaucurdenosapps",
          "tags": [
            "💡 Discovery"
          ],
          "type": "codelab",
          "links": []
        },
        {
          "__typename": "Session",
          "id": "day-1-lunch",
          "title": "Lunch",
          "complexity": null,
          "description": "Lunch",
          "language": "fr-FR",
          "startsAt": "2025-10-16T12:00",
          "endsAt": "2025-10-16T14:00",
          "rooms": [],
          "speakers": [],
          "feedbackId": null,
          "tags": [],
          "type": "lunch",
          "links": []
        }
      ]
    }
  }
}
"""

    val SESSION: String = """
{
  "data": {
    "session": {
      "__typename": "Session",
      "id": "thefutureofroutingwiththenavigationapi",
      "title": "The future of routing with the Navigation API",
      "complexity": "Intermediate",
      "description": "Today, virtually all Single Page Applications (SPAs) use a router. Each frontend framework offers one (or more), with often very similar APIs. Frontend routing is generally considered a solved problem. However, the reality is quite different: frontend routing relies on browser APIs that are frankly outdated.\n\nIn this presentation, we will explore the APIs that have shaped routing as we know it today. Then, we will discover what the future holds with the **Navigation API**, a promising new interface that could very well be a game-changer.",
      "language": "en-US",
      "startsAt": "2025-10-16T14:00",
      "endsAt": "2025-10-16T14:50",
      "rooms": [
        {
          "__typename": "Room",
          "id": "Tour Bretagne",
          "name": "Tour Bretagne"
        }
      ],
      "speakers": [
        {
          "__typename": "Speaker",
          "id": "eduardo_san_martin_morote",
          "bio": "Eduardo est développeur frontend freelance, membre à temps partiel de l’équipe Core de Vue.js, et auteur de libs telles que Vue Router, Pinia, VueFire, entre autres. Il est très impliqué dans l’open source et passionné par la création d’architectures frontend maintenables et testées.",
          "company": "Freelance",
          "companyLogoUrl": null,
          "city": "Paris, France",
          "name": "Eduardo San Martin Morote",
          "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/eduardo_san_martin_morote.jpg",
          "socials": [
            {
              "__typename": "Social",
              "name": "twitter",
              "url": "https://twitter.com/posva",
              "icon": null
            },
            {
              "__typename": "Social",
              "name": "linkedin",
              "url": "https://www.linkedin.com/posva",
              "icon": null
            }
          ]
        }
      ],
      "feedbackId": "lefuturduroutingaveclanavigationapi",
      "tags": [
        "🌍 Web"
      ],
      "type": "conference",
      "links": []
    }
  }
}
"""

    val SPEAKERS: String = """
{
  "data": {
    "speakers": [
      {
        "__typename": "Speaker",
        "id": "dasha_ilina",
        "bio": "Dasha Ilina is a Russian techno-critical artist based in Paris, France. Through the employment of low-tech and DIY approaches, her work questions the desire to incorporate modern technology into our daily lives by highlighting the implications of actually doing so.\n\nHer practice engages the public in order to facilitate a space for the development of critical thought regarding social imperatives for care of oneself and others, privacy in the digital age, and the reflexive contemporary urge to turn to technology for answers.\n\nShe is the founder of the Center for Technological Pain, a project that proposes DIY solutions to health problems caused by digital technologies for which she has received an Honorary Mention at Ars Electronica. Her project Technosommeil is in the collection of digital art works of the Département Val-de-Marne (Mallapixels). Ilina’s work has been exhibited at institutions such as Centre Pompidou (FR), MU Artspace (NL), Gaîté Lyrique (FR), Hartware Medienkunstverein Dortmund (DE), NeMe (CY), ISEA 2023 (FR) as well as various talks, workshops, and performances held internationally.\n\nShe is also the co-director of NØ SCHOOL, a summer school that focuses on critical research around the social and environmental impacts of information and communication technologies.",
        "company": "Artist",
        "companyLogoUrl": null,
        "city": "Paris, France",
        "name": "Dasha Ilina",
        "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/dasha_ilina.jpg",
        "socials": [
          {
            "__typename": "Social",
            "name": "linkedin",
            "url": "https://www.linkedin.com/dasha-ilina-633005132",
            "icon": null
          }
        ]
      },
      {
        "__typename": "Speaker",
        "id": "eduardo_san_martin_morote",
        "bio": "Eduardo est développeur frontend freelance, membre à temps partiel de l’équipe Core de Vue.js, et auteur de libs telles que Vue Router, Pinia, VueFire, entre autres. Il est très impliqué dans l’open source et passionné par la création d’architectures frontend maintenables et testées.",
        "company": "Freelance",
        "companyLogoUrl": null,
        "city": "Paris, France",
        "name": "Eduardo San Martin Morote",
        "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/eduardo_san_martin_morote.jpg",
        "socials": [
          {
            "__typename": "Social",
            "name": "twitter",
            "url": "https://twitter.com/posva",
            "icon": null
          },
          {
            "__typename": "Social",
            "name": "linkedin",
            "url": "https://www.linkedin.com/posva",
            "icon": null
          }
        ]
      },
      {
        "__typename": "Speaker",
        "id": "benjamin_bernard",
        "bio": "Développeur fullstack j'accorde beaucoup d'importance aux développements sous licences libres et plus globalement en Open Source, baignant de ce milieu depuis de nombreuses années au niveau associatif et professionnel.",
        "company": "Crédit Mutuel Arkéa",
        "companyLogoUrl": null,
        "city": "Brest, France",
        "name": "Benjamin Bernard",
        "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/benjamin_bernard.jpg",
        "socials": [
          {
            "__typename": "Social",
            "name": "linkedin",
            "url": "https://www.linkedin.com/benvii",
            "icon": null
          }
        ]
      },
      {
        "__typename": "Speaker",
        "id": "francois_nollen",
        "bio": "Staff Engineer chez SNCF Connect & Tech.\nMembre association « TOSIT : The Open Source I Trust » (https://tosit.fr).\nCommitter « Tock: The Open Conversation Kit » (https://doc.tock.ai). \n\nTopics: Flutter ChatBots OpenSource DevOps Observability Git Maven KotlinJVM...",
        "company": "SNCF Connect & Tech",
        "companyLogoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/srcsncf_connect___tech.png",
        "city": "Paris, France",
        "name": "François Nollen",
        "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/francois_nollen.jpg",
        "socials": [
          {
            "__typename": "Social",
            "name": "github",
            "url": "https://github.com/francoisno",
            "icon": null
          },
          {
            "__typename": "Social",
            "name": "twitter",
            "url": "https://twitter.com/FrancoisN0",
            "icon": null
          },
          {
            "__typename": "Social",
            "name": "linkedin",
            "url": "https://www.linkedin.com/francois-nollen",
            "icon": null
          }
        ]
      },
      {
        "__typename": "Speaker",
        "id": "pierre_therrode",
        "bio": "**Lead Developer** chez **SNCF Connect & Tech** sur la partie chatbot depuis près de 5 ans.\nGeek et curieux, j'aime comprendre le fonctionnement des choses qui nous entourent.\nContributeur à la plateforme open source [Tock - The Open Conversation Kit](https://doc.tock.ai/)",
        "company": "SNCF Connect & Tech",
        "companyLogoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/srcsncf_connect___tech.png",
        "city": "Paris, France",
        "name": "Pierre Therrode",
        "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/pierre_therrode.jpg",
        "socials": [
          {
            "__typename": "Social",
            "name": "github",
            "url": "https://github.com/pi-2r",
            "icon": null
          },
          {
            "__typename": "Social",
            "name": "twitter",
            "url": "https://twitter.com/pi2r3",
            "icon": null
          },
          {
            "__typename": "Social",
            "name": "linkedin",
            "url": "https://www.linkedin.com/ptherrode",
            "icon": null
          }
        ]
      },
      {
        "__typename": "Speaker",
        "id": "oceane_gillard",
        "bio": "Dev web fullstack et mobile, j'ai de multiples casquettes et aime garder une vision globale sur les projets.\nAyant découvert Flutter en 2020, j'ai tout de suite adoré ! J'ai d'ailleurs développé une application Flutter permettant de compter les points durant une partie de Barbu (un jeu de cartes, aussi connu sous le nom de Tafferan). L'application Score Barbu est disponible sur le PlayStore depuis 2023.\n\nJ'ai rejoint Zenika en 2023, ce qui m'a permis de participer à de nombreux projets et donner mes premières conférences.",
        "company": "Zenika",
        "companyLogoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/srczenika.png",
        "city": "Nantes, France",
        "name": "Océane Gillard",
        "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/oceane_gillard.jpg",
        "socials": [
          {
            "__typename": "Social",
            "name": "linkedin",
            "url": "https://www.linkedin.com/oceane-gillard",
            "icon": null
          },
          {
            "__typename": "Social",
            "name": "github",
            "url": "https://github.com/Zigotote",
            "icon": null
          }
        ]
      },
      {
        "__typename": "Speaker",
        "id": "patrice_de_saint_steban",
        "bio": "Depuis plus de 15 ans, je me passionne pour le développement d'applications WEB et Mobile, en touchant à toutes les couches du Front au Back jusqu'à son déploiement sur le Cloud.\n\nJ'ai travaillé sur des technologies très différentes, du PHP au JAVA et JAVASCRIPT, du GWT à Angular et React, de Google App Engine à Node.js, mais également le mobile avec Flutter et l'accessibilité.\nJe suis organisateur du Meetup Flutter Nantes, créateur d'un plugin IntelliJ et de site WordPress pour des associations.",
        "company": "Zenika",
        "companyLogoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/srczenika.png",
        "city": "Nantes, France",
        "name": "Patrice De Saint Steban",
        "photoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/speakers/patrice_de_saint_steban.jpg",
        "socials": [
          {
            "__typename": "Social",
            "name": "github",
            "url": "https://github.com/patou",
            "icon": null
          },
          {
            "__typename": "Social",
            "name": "bluesky",
            "url": "",
            "icon": null
          },
          {
            "__typename": "Social",
            "name": "linkedin",
            "url": "https://www.linkedin.com/patrice-de-saint-steban",
            "icon": null
          }
        ]
      }
    ]
  }
}
"""

    val PARTNER_GROUPS: String = """
{
  "data": {
    "partnerGroups": [
      {
        "__typename": "PartnerGroup",
        "title": "PXL",
        "partners": [
          {
            "__typename": "Partner",
            "logoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main//src/images/partners/dataiku.png",
            "name": "Dataiku",
            "url": "https://www.dataiku.com/"
          }
        ]
      },
      {
        "__typename": "PartnerGroup",
        "title": "Platinium",
        "partners": [
          {
            "__typename": "Partner",
            "logoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main//src/images/partners/proginov.png",
            "name": "Proginov",
            "url": "https://www.proginov.com/"
          }
        ]
      },
      {
        "__typename": "PartnerGroup",
        "title": "Gold",
        "partners": [
          {
            "__typename": "Partner",
            "logoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main//src/images/partners/epitech.png",
            "name": "Epitech",
            "url": "https://www.epitech.eu/"
          }
        ]
      },
      {
        "__typename": "PartnerGroup",
        "title": "Virtual",
        "partners": [
          {
            "__typename": "Partner",
            "logoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main//src/images/partners/sully-group.png",
            "name": "Sully Group",
            "url": "https://www.sully-group.com/"
          }
        ]
      },
      {
        "__typename": "PartnerGroup",
        "title": "Velotypie",
        "partners": [
          {
            "__typename": "Partner",
            "logoUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main//src/images/partners/arkup.png",
            "name": "Arkup",
            "url": "https://www.arkup.fr/arkup/"
          }
        ]
      }
    ]
  }
}
"""

    val VENUE_EN: String = """
{
  "data": {
    "venue": {
      "__typename": "Venue",
      "name": "Cité des Congrès de Nantes",
      "address": "5 rue de Valmy, 44000 Nantes",
      "description": "Located in the center of Nantes, the event takes place in the \"Cité des Congrès\" with more than 3000m² of conference rooms, hand's on and networking space…",
      "latitude": 47.21308725112951,
      "longitude": -1.542622837466317,
      "imageUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/home/album/equipe-amphi.jpg",
      "floorPlanUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/plan-cite-transparent.png"
    }
  }
}
"""

    val VENUE_FR: String = """
{
  "data": {
    "venue": {
      "__typename": "Venue",
      "name": "Cité des Congrès de Nantes",
      "address": "5 rue de Valmy, 44000 Nantes",
      "description": "Située en plein cœur de ville, La Cité des Congrès de Nantes propose pour le DevFest Nantes plus de 3000m² de salles de conférences, codelabs et lieu de rencontre…",
      "latitude": 47.21308725112951,
      "longitude": -1.542622837466317,
      "imageUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/home/album/equipe-amphi.jpg",
      "floorPlanUrl": "https://raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/images/plan-cite-transparent.png"
    }
  }
}
"""
}

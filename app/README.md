# A6: At Your Service

## Overview
This project is our group's Android app for the **“At Your Service”** assignment. It demonstrates:
1. Our group’s name displayed on the main screen (`MainActivity`).
2. A button on the main screen that navigates to the **API Activity** (`ApiActivity`) which calls a free public web service.
3. A placeholder button for future Firebase integration (`FirebaseActivity`).

We fetch cryptocurrency data (name, symbol, price, icon) from multiple APIs (CoinCap, CoinPaprika, CoinGecko) and display the results in a `RecyclerView`, including dynamically loaded images.

---

## How It Meets Assignment Requirements

1. **New App & Repo**
    - Created a brand-new Android Studio project within a new, private GitHub repository, named for our group.

2. **Group Name in Main Activity**
    - `MainActivity` displays our group’s name.

3. **Button to Launch a New Activity**
    - `MainActivity` includes a button labeled “Go to API Screen,” opening `ApiActivity`.

4. **Usage of a Public Web Service**
    - We make network requests via `HttpURLConnection` on a background thread (not blocking the UI).
    - We **do not** use external networking libraries (e.g., Volley, OkHttp).

5. **UI for Input/Output**
    - `ApiActivity` has:
        - A **Spinner** to pick from CoinPaprika, CoinCap, or CoinGecko.
        - A **“Fetch Crypto Data”** button.
        - An animated **“Loading...”** text to indicate a network call in progress.
        - A `RecyclerView` to present coin data (name, symbol, price, icon).

6. **All Five Challenge Items**
    1. **Icons for Categorical Variables**  
       Dynamically loading coin icons based on symbol.
    2. **Variable-Length List in a RecyclerView**  
       Different APIs may return differing counts of coins (e.g., top 5, or just Bitcoin).
    3. **Variable-Length Strings**  
       Coins can have names/symbols of various lengths, and the layout adapts accordingly.
    4. **Dynamically Adjusted Layout**  
       Using a `RecyclerView` (plus layout managers) ensures it expands/shrinks for any number of items and works well in both orientations.
    5. **Multiple UI Controls**  
       Spinner for API selection, plus a separate “Fetch” button; the user can re-fetch with different parameters.

7. **Active Indicator While Fetching**
    - A “Loading” text that cycles through dots until the data is received.

8. **Portrait & Landscape Compatibility**
    - We tested in both orientations, ensuring the layout remains user-friendly (scrolling if needed).

9. **Smooth UI & Error Handling**
    - Any failures (network, JSON parsing) appear via a **Snackbar**.
    - The back button returns to `MainActivity` as expected.

10. **No AsyncTask**
    - All network operations use threads and handlers, avoiding the deprecated `AsyncTask`.

---

## Usage

1. **Launch the App**: The main screen shows the group name and two buttons.
2. **API Screen**: Tap **“Go to API Screen.”**
3. **Select API Source**: Choose CoinGecko (Bitcoin), CoinPaprika (top 5), or CoinCap (top 5) in the Spinner.
4. **Fetch**: Tap **“Fetch Crypto Data.”**
    - A “Loading...” text animates until data arrives.
5. **View Results**: The `RecyclerView` shows each coin’s name, symbol, price, and icon.

---

## Contributors

| Name         | Contributions                                                                       |
|--------------|-------------------------------------------------------------------------------------|
| **Willem**   | @willem | Created the base project, ensured it met initial requirements, and completed the first two challenge items. |
| **Madisnen** | Implemented challenge #3, major UI overhaul/improvements.       |
| **Rohan**    | Implemented challenge #4, handled UI issues in portrait/landscape orientation.           |
| **Parwaz**   | Implemented challenge #5, ensured no usage of deprecated AsyncTask.                               |
| **Yunmu**    | Assisted in all challenge tasks, reviewed everyone’s work for clarity and consistency.                    |

---

## Next Steps
- Implement Firebase Realtime Database in `FirebaseActivity` in a future assignment.

---

## License
This project is released under the [MIT License](LICENSE).  
Feel free to adapt or redistribute for educational purposes.

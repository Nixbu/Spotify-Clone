(function() {
    document.addEventListener('DOMContentLoaded', function() {
        // Get CSRF token
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

        // Handle favorite button clicks
        document.querySelectorAll('.favorite-btn').forEach(button => {
            button.addEventListener('click', function(e) {
                e.preventDefault();
                const songId = this.dataset.songId;
                const isCurrentlyFavorite = this.classList.contains('btn-danger');

                // Determine API endpoint and method
                const url = `/api/favorites/${songId}`;
                const method = isCurrentlyFavorite ? 'DELETE' : 'POST';

                // Prepare headers
                const headers = {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                };

                // Add CSRF token if available
                if (csrfToken && csrfHeader) {
                    headers[csrfHeader] = csrfToken;
                }

                fetch(url, {
                    method: method,
                    headers: headers
                })
                    .then(response => {
                        if (response.ok) {
                            return response.text();
                        }
                        throw new Error('Network response was not ok');
                    })
                    .then(result => {
                        console.log('Success:', result);

                        // Update button appearance
                        if (isCurrentlyFavorite) {
                            this.classList.remove('btn-danger');
                            this.classList.add('btn-outline-secondary');
                            showToast('Song removed from favorites!', 'success');
                        } else {
                            this.classList.remove('btn-outline-secondary');
                            this.classList.add('btn-danger');
                            showToast('Song added to favorites!', 'success');
                        }
                    })
                    .catch(error => {
                        console.error('Error:', error);
                        showToast('Failed to update favorites', 'error');
                    });
            });
        });

        // Load favorite status for all songs
        loadFavoriteStatus();
    });

    function loadFavoriteStatus() {
        const favoriteButtons = document.querySelectorAll('.favorite-btn');

        favoriteButtons.forEach(button => {
            const songId = button.dataset.songId;

            fetch(`/api/favorites`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                }
            })
                .then(response => response.json())
                .then(favorites => {
                    const isFavorite = favorites.some(song => song.id == songId);
                    if (isFavorite) {
                        button.classList.remove('btn-outline-secondary');
                        button.classList.add('btn-danger');
                    }
                })
                .catch(error => {
                    console.error('Error loading favorite status:', error);
                });
        });
    }

    function showToast(message, type = 'success') {
        const toast = document.createElement('div');
        toast.className = `alert alert-${type === 'success' ? 'success' : type === 'error' ? 'danger' : 'info'} alert-dismissible fade show`;
        toast.style.position = 'fixed';
        toast.style.top = '20px';
        toast.style.right = '20px';
        toast.style.zIndex = '9999';
        toast.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        document.body.appendChild(toast);

        setTimeout(() => {
            toast.remove();
        }, 3000);
    }
})();
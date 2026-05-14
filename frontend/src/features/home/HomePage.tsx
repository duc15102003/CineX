export default function HomePage() {
  return (
    <div className="min-h-screen bg-gray-950 text-white">
      <header className="flex items-center justify-between px-8 py-4 border-b border-gray-800">
        <h1 className="text-2xl font-bold text-amber-400">CineX</h1>
        <nav className="flex gap-6 text-sm">
          <a href="/" className="hover:text-amber-400 transition-colors">
            Trang chu
          </a>
          <a href="/movies" className="hover:text-amber-400 transition-colors">
            Phim
          </a>
          <a href="/login" className="hover:text-amber-400 transition-colors">
            Dang nhap
          </a>
        </nav>
      </header>

      <main className="flex flex-col items-center justify-center px-8 py-24">
        <h2 className="text-5xl font-bold mb-6 text-center">
          Dat ve xem phim <span className="text-amber-400">truc tuyen</span>
        </h2>
        <p className="text-gray-400 text-lg mb-8 text-center max-w-2xl">
          He thong dat ve xem phim online tien loi. Chon phim, chon suat, chon
          ghe va thanh toan chi trong vai buoc.
        </p>
        <button className="bg-amber-500 hover:bg-amber-600 text-black font-semibold px-8 py-3 rounded-lg transition-colors">
          Kham pha ngay
        </button>
      </main>

      <footer className="text-center text-gray-600 text-sm py-8 border-t border-gray-800">
        &copy; 2026 CineX. Do an tot nghiep.
      </footer>
    </div>
  )
}

import React from "react";

export default function Footer() {

    const [year] = React.useState(new Date().getFullYear());

    return (
        <footer className="row-start-3 flex gap-6 flex-wrap items-center justify-center">
            <div className="space-x-2">
                <a>Meta</a>
                <a>About</a>
                <a>Blog</a>
                <a>Jobs</a>
                <a>Help</a>
                <a>API</a>
                <a>Privacy</a>
                <a>Terms</a>
                <a>Locations</a>
                <a>Instagram Lite</a>
                <a>Threads</a>
                <a>Contact Uploading & Non-Users</a>
                <a>Meta Verified</a>
            </div>
            <div>Técnicas de Teste - © {year} Instagram Clone from Paulo Pereira - IFPB</div>
        </footer>
    );
}
